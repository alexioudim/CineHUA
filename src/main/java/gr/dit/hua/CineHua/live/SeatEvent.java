package gr.dit.hua.CineHua.live;

import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatEvent {
    public enum Type { SELECT, RENEW, RELEASE, SOLD }

    private Type type;
    private long screeningId;
    private long seatAvailabilityId;
    private Long holderUserId;     // null σε RELEASE όταν φεύγει
    private long ttlSeconds;       // -1 αν δεν ισχύει
    private Instant at;
}