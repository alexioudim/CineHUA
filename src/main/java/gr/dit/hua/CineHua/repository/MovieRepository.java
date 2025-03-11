package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

}
