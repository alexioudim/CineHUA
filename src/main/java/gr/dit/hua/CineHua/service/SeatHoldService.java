package gr.dit.hua.CineHua.service;


import gr.dit.hua.CineHua.live.SeatLivePublisher;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final RedissonClient redisson;
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final SeatLivePublisher live;

    private static final String SEM_PREFIX    = "seat:sem:";       // seat:sem:{saId}
    private static final String HOLDS_BY_USER = "seat:holds:";     // seat:holds:{userId} -> {saId -> permitId} (MapCache)
    private static final String HOLDER_PREFIX = "seat:holder:";    // seat:holder:{saId} -> userId (Bucket TTL)
    private static final String PERMIT_PREFIX = "seat:permit:";    // seat:permit:{saId} -> permitId (Bucket TTL)

    /** Acquire hold (no wait). Returns permit info with expiresAt if success. */
    public Optional<SeatHold> tryHold(long saId, long userId, long ttlSec) throws InterruptedException {
        var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
        sem.trySetPermits(1);

        // waitTime=0 (no wait), leaseTime=ttlSec
        String permitId = sem.tryAcquire(0L, ttlSec, TimeUnit.SECONDS);
        if (permitId == null) return Optional.empty();

        // Write all related keys atomically; if it fails, rollback the permit.
        try {
            RBatch batch = redisson.createBatch();
            // Per-user reverse index with entry TTL
            batch.getMapCache(HOLDS_BY_USER + userId)
                    .fastPutAsync(String.valueOf(saId), permitId, ttlSec, TimeUnit.SECONDS);
            // Seat-scoped holder (for UI coloring)
            batch.getBucket(HOLDER_PREFIX + saId)
                    .setAsync(String.valueOf(userId), ttlSec, TimeUnit.SECONDS);
            // Seat-scoped permit (fallback on release)
            batch.getBucket(PERMIT_PREFIX + saId)
                    .setAsync(permitId, ttlSec, TimeUnit.SECONDS);
            batch.execute();
        } catch (Exception e) {
            sem.release(permitId); // rollback
            throw e;
        }

        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishSelect(screeningId, saId, userId, ttlSec);

        return Optional.of(new SeatHold(saId, permitId, Instant.now().plusSeconds(ttlSec)));
    }

    /** Heartbeat renew: extends semaphore lease + holder/permit TTLs. Auto-cleans if expired. */
    public boolean renewHold(long saId, long userId, long ttlSec) {
        RMapCache<String, String> userMap = redisson.getMapCache(HOLDS_BY_USER + userId);
        String permitId = userMap.get(String.valueOf(saId));
        if (permitId == null) return false;

        var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
        boolean ok = sem.updateLeaseTime(permitId, ttlSec, TimeUnit.SECONDS);
        if (!ok) {
            // Stale reverse index: clean everything
            userMap.remove(String.valueOf(saId));
            redisson.<String>getBucket(HOLDER_PREFIX + saId).delete();
            redisson.<String>getBucket(PERMIT_PREFIX + saId).delete();
            return false;
        }

        // Keep seat-scoped TTLs in sync
        RBucket<String> holder = redisson.getBucket(HOLDER_PREFIX + saId);
        if (String.valueOf(userId).equals(holder.get())) {
            holder.expire(Duration.ofSeconds(ttlSec));
        }
        redisson.getBucket(PERMIT_PREFIX + saId).expire(Duration.ofSeconds(ttlSec));

        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishRenew(screeningId, saId, userId, ttlSec);
        return true;
    }

    /** Release hold safely: remove indexes, release by permit (with fallback), and sanity reset if stuck. */
    public void releaseHold(long saId, long userId) {
        RMapCache<String, String> userMap = redisson.getMapCache(HOLDS_BY_USER + userId);
        String permitId = userMap.remove(String.valueOf(saId));

        RBucket<String> holderBucket = redisson.getBucket(HOLDER_PREFIX + saId);
        String currentHolder = holderBucket.get();

        // Fallback: if reverse map lacks permitId, try seat-scoped permit bucket
        if (permitId == null) {
            permitId = redisson.<String>getBucket(PERMIT_PREFIX + saId).get();
        }

        // Remove holder if truly owned by this user
        if (String.valueOf(userId).equals(currentHolder)) {
            holderBucket.delete();
        }

        // Remove the seat-scoped permit bucket (not needed anymore)
        redisson.getBucket(PERMIT_PREFIX + saId).delete();

        // Release semaphore permit if we have it
        var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
        if (permitId != null) {
            try { sem.release(permitId); } catch (Exception ignore) { /* already expired/released */ }
        }

        // Sanity reset: if no holder and no available permits, reset semaphore to 1 permit
        boolean hasHolder = redisson.<String>getBucket(HOLDER_PREFIX + saId).isExists();
        int avail = sem.availablePermits();
        if (!hasHolder && avail == 0) {
            redisson.getKeys().delete(SEM_PREFIX + saId);
            redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId).trySetPermits(1);
        }

        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishRelease(screeningId, saId);
    }

    /** Reverse index: what this user holds right now. */
    public Map<Long, String> getUserHolds(long userId) {
        RMapCache<String, String> userMap = redisson.getMapCache(HOLDS_BY_USER + userId);
        Map<Long, String> res = new HashMap<>();
        userMap.forEach((k, v) -> res.put(Long.parseLong(k), v));
        return res;
    }

    /** Who holds this seat now (if any)? */
    public Optional<Long> getSeatHolder(long saId) {
        String v = redisson.<String>getBucket(HOLDER_PREFIX + saId).get();
        if (v == null) return Optional.empty();
        try { return Optional.of(Long.parseLong(v)); } catch (NumberFormatException e) { return Optional.empty(); }
    }

    /** Remaining TTL for holder key (seconds). -1 if none. */
    public long getSeatHoldTtl(long saId) {
        long ms = redisson.<String>getBucket(HOLDER_PREFIX + saId).remainTimeToLive();
        return ms < 0 ? -1 : ms / 1000;
    }

    @Data
    @AllArgsConstructor
    public static class SeatHold {
        long seatAvailabilityId;
        String permitId;
        Instant expiresAt;
    }
}
