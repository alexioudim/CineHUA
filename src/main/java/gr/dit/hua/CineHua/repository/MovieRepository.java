package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Movie findById(long id);
    List<Movie> findByIsPublicTrue();
}
