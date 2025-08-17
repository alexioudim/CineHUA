package gr.dit.hua.CineHua.dto.request;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PosStartRequest {
    private List<Long> seatAvailabilityIds;
}