package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByMovieId(long Id);
}
