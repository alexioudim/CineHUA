package gr.dit.hua.CineHua.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class BookingRequest {

    private long issuerId;
    private List<Long> seatAvailabilitiyIdList;

    public long getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(long issuerId) {
        this.issuerId = issuerId;
    }

    public List<Long> getSeatAvailabilitiyIdList() {
        return seatAvailabilitiyIdList;
    }

    public void setSeatAvailabilitiyIdList(List<Long> seatAvailabilitiyIdList) {
        this.seatAvailabilitiyIdList = seatAvailabilitiyIdList;
    }
}
