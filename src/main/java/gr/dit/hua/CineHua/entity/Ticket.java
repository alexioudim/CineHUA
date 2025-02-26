package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ticketId;

    private float price;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @ManyToOne
    private Screening screening;

    @ManyToOne
    private Seat seat;

    @ManyToOne
    private Booking booking;
}

enum TicketStatus {
    AVAILABLE, BOOKED
}
