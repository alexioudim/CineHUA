package gr.dit.hua.CineHua.dto.request;

import gr.dit.hua.CineHua.entity.TicketStatus;

import java.math.BigDecimal;

public class TicketRequest {

    private long ticketId;
    private BigDecimal price;
    private TicketStatus status;

    public long getTicketId() {
        return ticketId;
    }

    public void setTicketId(long ticketId) {
        this.ticketId = ticketId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
