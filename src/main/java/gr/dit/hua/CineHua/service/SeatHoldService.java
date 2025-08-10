package gr.dit.hua.CineHua.service;


import gr.dit.hua.CineHua.live.SeatLivePublisher;
import gr.dit.hua.CineHua.repository.ScreeningRepository;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final RedissonClient redisson;

    private static final String SEM_PREFIX = "seat:sem:";          // seat:sem:{saId}
    private static final String HOLDS_BY_USER = "seat:holds:";     // seat:holds:{userId} -> {saId -> permitId}
    private static final String HOLDER_PREFIX = "seat:holder:";    // seat:holder:{saId} -> userId (TTL)
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final SeatLivePublisher live;

    public Optional<SeatHold> tryHold(long saId, long userId, long ttlSec) throws InterruptedException {
        var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
        sem.trySetPermits(1);

        String permitId = sem.tryAcquire(0L, ttlSec, TimeUnit.SECONDS);
        if (permitId == null) return Optional.empty();

        try {
            var batch = redisson.createBatch();
            batch.getMapCache(HOLDS_BY_USER + userId)
                    .fastPutAsync(String.valueOf(saId), permitId, ttlSec, TimeUnit.SECONDS);
            batch.getBucket(HOLDER_PREFIX + saId)
                    .setAsync(String.valueOf(userId), ttlSec, TimeUnit.SECONDS);
            batch.execute();
        } catch (Exception e) {
            // rollback permit if Redis writes failed
            sem.release(permitId);
            throw e;
        }
        // publish
        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishSelect(screeningId, saId, userId, ttlSec);

        return Optional.of(new SeatHold(saId, permitId, Instant.now().plusSeconds(ttlSec)));
    }

    public boolean renewHold(long saId, long userId, long ttlSec) {
        var userMap = redisson.<String, String>getMap(HOLDS_BY_USER + userId);
        String permitId = userMap.get(String.valueOf(saId));
        if (permitId == null) return false;

        var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
        boolean ok = sem.updateLeaseTime(permitId, ttlSec, TimeUnit.SECONDS);
        if (!ok) {
            // cleanup stale reverse index + holder
            userMap.remove(String.valueOf(saId));
            var holder = redisson.<String>getBucket(HOLDER_PREFIX + saId);
            if (String.valueOf(userId).equals(holder.get())) {
                holder.delete();
            }
            return false;
        }
        var holder = redisson.<String>getBucket(HOLDER_PREFIX + saId);
        if (String.valueOf(userId).equals(holder.get())) {
            holder.expire(Duration.ofSeconds(ttlSec));
        }

        // publish
        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishRenew(screeningId, saId, userId, ttlSec);
        return true;
    }

    public void releaseHold(long saId, long userId) {
        var userMap = redisson.<String, String>getMap(HOLDS_BY_USER + userId);
        String permitId = userMap.remove(String.valueOf(saId));

        // σβήσε seat holder μόνο αν όντως το κρατούσε αυτός ο χρήστης
        var holderBucket = redisson.<String>getBucket(HOLDER_PREFIX + saId);
        String currentHolder = holderBucket.get();
        if (String.valueOf(userId).equals(currentHolder)) {
            holderBucket.delete();
        }

        if (permitId != null) {
            var sem = redisson.getPermitExpirableSemaphore(SEM_PREFIX + saId);
            sem.release(permitId);
        }

        // publish
        Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(saId);
        live.publishRelease(screeningId, saId);
    }

    public Map<Long, String> getUserHolds(long userId) {
        var userMap = redisson.<String, String>getMap(HOLDS_BY_USER + userId);
        Map<Long, String> res = new HashMap<>();
        userMap.forEach((k, v) -> res.put(Long.parseLong(k), v));
        return res;
    }

    /** Ποιος κρατάει τη θέση τώρα (αν κάποιος); */
    public Optional<Long> getSeatHolder(long saId) {
        var holderBucket = redisson.<String>getBucket(HOLDER_PREFIX + saId);
        String v = holderBucket.get();
        if (v == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(v));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Πόσα δευτερόλεπτα TTL απομένουν στο holder (για countdown UI). -1 αν δεν υπάρχει TTL/holder. */
    public long getSeatHoldTtl(long saId) {
        var holderBucket = redisson.<String>getBucket(HOLDER_PREFIX + saId);
        long ms = holderBucket.remainTimeToLive();
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
