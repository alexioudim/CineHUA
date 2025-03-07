package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;
import org.springframework.boot.availability.AvailabilityState;

@Entity
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long availabilityId;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    @ManyToOne
    private Seat seat;

    @ManyToOne
    private Screening screening;
}

enum AvailabilityStatus {
    AVAILABLE, SELECTED, HOUSE, SOLD
}
