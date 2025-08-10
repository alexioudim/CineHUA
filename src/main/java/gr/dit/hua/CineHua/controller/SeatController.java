package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.entity.AvailabilityStatus;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import gr.dit.hua.CineHua.service.SeatHoldService;
import gr.dit.hua.CineHua.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {
    private final SeatHoldService seatHoldService;
    private final SeatAvailabilityRepository seatAvailabilityRepository;

    @PostMapping("/select/{id}")
    public ResponseEntity<?> select(@PathVariable long id, @AuthenticationPrincipal UserDetailsImpl user) throws InterruptedException {
        return seatHoldService.tryHold(id, user.getId(), 120)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(409).body("Seat already held"));
    }

    @PostMapping("/renew/{id}")
    public ResponseEntity<?> renewSeat (@PathVariable long id, @AuthenticationPrincipal UserDetailsImpl user) {
        return seatHoldService.renewHold(id, user.getId(), 120)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(409).build();
    }

    @DeleteMapping("/release/{id}")
    public ResponseEntity<?> releaseSeat(@PathVariable long id, @AuthenticationPrincipal UserDetailsImpl user)  {
        seatHoldService.releaseHold(id, user.getId());
        return ResponseEntity.ok().build();
    }

    // 4) Bulk select
    @PostMapping("/select/bulk")
    public ResponseEntity<BulkSelectResponse> bulkSelect(@RequestBody BulkSelectRequest req,
                                                         @AuthenticationPrincipal UserDetailsImpl user) throws InterruptedException {
        long userId = user.getId();
        List<Long> acquired = new ArrayList<>();
        List<Long> conflicts = new ArrayList<>();
        for (Long saId : req.seatAvailabilityIds()) {
            if (seatHoldService.tryHold(saId, userId, req.ttlSeconds()).isPresent()) acquired.add(saId);
            else conflicts.add(saId);
        }
        return ResponseEntity.ok(new BulkSelectResponse(acquired, conflicts));
    }

    // 5) Renew all my holds
    @PostMapping("/renew/all")
    public ResponseEntity<?> renewAll(@AuthenticationPrincipal UserDetailsImpl user,
                                      @RequestParam(defaultValue = "120") long ttlSeconds) {
        long userId = user.getId();
        var holds = seatHoldService.getUserHolds(userId);
        boolean allOk = true;
        for (var e : holds.entrySet()) {
            boolean ok = seatHoldService.renewHold(e.getKey(), userId, ttlSeconds);
            if (!ok) allOk = false;
        }
        return allOk ? ResponseEntity.ok().build() : ResponseEntity.status(207).body("Some holds expired");
    }

    // 6) Release all my holds
    @DeleteMapping("/release/all")
    public ResponseEntity<?> releaseAll(@AuthenticationPrincipal UserDetailsImpl user) {
        long userId = user.getId();
        var holds = seatHoldService.getUserHolds(userId);
        for (Long saId : holds.keySet()) seatHoldService.releaseHold(saId, userId);
        return ResponseEntity.ok().build();
    }

    // 7) What do I currently hold?
    @GetMapping("/me")
    public ResponseEntity<List<Long>> myHolds(@AuthenticationPrincipal UserDetailsImpl user) {
        long userId = user.getId();
        return ResponseEntity.ok(new ArrayList<>(seatHoldService.getUserHolds(userId).keySet()));
    }

    // 8) Screening status for UI coloring (AVAILABLE / HELD_BY_ME / HELD_BY_OTHER / SOLD)
    @GetMapping("/screening/{screeningId}/status")
    public ResponseEntity<List<SeatStatusDto>> screeningStatus(@PathVariable long screeningId,
                                                               @AuthenticationPrincipal UserDetailsImpl principal) {
        long userId = principal.getId();

        List<SeatAvailability> list = seatAvailabilityRepository.findAllByScreeningId(screeningId);
        List<SeatStatusDto> out = new ArrayList<>(list.size());

        for (SeatAvailability sa : list) {
            if (sa.getAvailability() == AvailabilityStatus.SOLD) {
                out.add(new SeatStatusDto(sa.getId(), SeatState.SOLD, -1));
                continue;
            }
            var holderOpt = seatHoldService.getSeatHolder(sa.getId());
            if (holderOpt.isEmpty()) {
                out.add(new SeatStatusDto(sa.getId(), SeatState.AVAILABLE, -1));
            } else {
                long ttl = seatHoldService.getSeatHoldTtl(sa.getId());
                SeatState st = holderOpt.get() == userId ? SeatState.HELD_BY_ME : SeatState.HELD_BY_OTHER;
                out.add(new SeatStatusDto(sa.getId(), st, ttl));
            }
        }
        return ResponseEntity.ok(out);

    }

    public record BulkSelectRequest(List<Long> seatAvailabilityIds, long ttlSeconds) {}
    public record BulkSelectResponse(List<Long> acquired, List<Long> conflicts) {}
    public record SeatStatusDto(Long seatAvailabilityId, SeatState state, long ttlSeconds) {}
    public enum SeatState { AVAILABLE, HELD_BY_ME, HELD_BY_OTHER, SOLD }
}
