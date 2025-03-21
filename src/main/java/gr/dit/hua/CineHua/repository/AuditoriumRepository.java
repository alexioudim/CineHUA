package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Auditorium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    Auditorium findById(long auditorium_id);
}
