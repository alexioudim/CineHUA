package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Screening;
import gr.dit.hua.CineHua.entity.Seat;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    SeatAvailability findBySeat(Seat seat);

    SeatAvailability findByScreening(Screening screening);

    List<SeatAvailability> findAllByScreening(Screening screening);
}
