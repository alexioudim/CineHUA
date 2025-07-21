package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticket_id;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @OneToOne
    @JoinColumn(name = "seat_availability_id")
    private SeatAvailability seatAvailability;

    @ManyToOne(optional = false)
    private Booking booking;

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

    public SeatAvailability getSeatAvailability() {
        return seatAvailability;
    }

    public void setSeatAvailability(SeatAvailability seatAvailability) {
        this.seatAvailability = seatAvailability;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Long getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(Long ticket_id) {
        this.ticket_id = ticket_id;
    }
}

