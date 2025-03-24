package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
