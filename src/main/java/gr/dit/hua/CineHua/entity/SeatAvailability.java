package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;
import org.springframework.boot.availability.AvailabilityState;

@Entity
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availability_id;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;
}

enum AvailabilityStatus {
    AVAILABLE, SELECTED, HOUSE, SOLD
}
