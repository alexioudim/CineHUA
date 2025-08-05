package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Auditorium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    Auditorium findById(long id);
}
