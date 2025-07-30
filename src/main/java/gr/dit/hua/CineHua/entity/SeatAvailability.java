package gr.dit.hua.CineHua.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.boot.availability.AvailabilityState;

@Entity
public class SeatAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availability_id;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    public SeatAvailability() {
    }

    public Long getAvailability_id() {
        return availability_id;
    }

    public void setAvailability_id(Long availability_id) {
        this.availability_id = availability_id;
    }

    public AvailabilityStatus getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityStatus availability) {
        this.availability = availability;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public Screening getScreening() {
        return screening;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;
    }
}



