package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Movie findById(long id) {
        return movieRepository.findById(id);
    }

    @Transactional
    public List<Movie> findAllMovies() {
        return movieRepository.findAll();
    }

    @Transactional
    public void saveMovie(Movie movie) {
        movieRepository.save(movie);

    }

    @Transactional
    public void deleteMovie(long id) {
        Movie movie = movieRepository.findById(id);
        movieRepository.delete(movie);
    }



}
