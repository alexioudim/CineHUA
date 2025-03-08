package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    Seat findByAuditorium(Auditorium auditorium);
    List<Seat> findAllByAuditorium(Auditorium auditorium);
}
