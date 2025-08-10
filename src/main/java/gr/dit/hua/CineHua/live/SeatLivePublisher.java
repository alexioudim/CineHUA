package gr.dit.hua.CineHua.live;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SeatLivePublisher {
    private final SeatSseHub hub;

    public void publishSelect(long screeningId, long saId, long userId, long ttlSec) {
        hub.publish(SeatEvent.builder()
                .type(SeatEvent.Type.SELECT)
                .screeningId(screeningId)
                .seatAvailabilityId(saId)
                .holderUserId(userId)
                .ttlSeconds(ttlSec)
                .at(Instant.now())
                .build());
    }

    public void publishRenew(long screeningId, long saId, long userId, long ttlSec) {
        hub.publish(SeatEvent.builder()
                .type(SeatEvent.Type.RENEW)
                .screeningId(screeningId)
                .seatAvailabilityId(saId)
                .holderUserId(userId)
                .ttlSeconds(ttlSec)
                .at(Instant.now())
                .build());
    }

    public void publishRelease(long screeningId, long saId) {
        hub.publish(SeatEvent.builder()
                .type(SeatEvent.Type.RELEASE)
                .screeningId(screeningId)
                .seatAvailabilityId(saId)
                .holderUserId(null)
                .ttlSeconds(-1)
                .at(Instant.now())
                .build());
    }

    public void publishSold(long screeningId, long saId) {
        hub.publish(SeatEvent.builder()
                .type(SeatEvent.Type.SOLD)
                .screeningId(screeningId)
                .seatAvailabilityId(saId)
                .holderUserId(null)
                .ttlSeconds(-1)
                .at(Instant.now())
                .build());
    }
}

