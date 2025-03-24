package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByAuditorium(Auditorium auditorium);
}
