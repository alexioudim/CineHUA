package gr.dit.hua.CineHua.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.repository.MovieRepository;
import gr.dit.hua.CineHua.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/all")
    public List<Movie> getMovies() {
        return movieService.listAll();
    }

    @GetMapping("/all/public")
    public List<Movie> getPublicMovies() {
        return movieService.listPublic();
    }

    @PostMapping("/new/{imdb_id}")
    public ResponseEntity<String> createMovie(@PathVariable("imdb_id") String imdbId) {
        try {
            Movie saved = movieService.createFromImdb(imdbId);
            return ResponseEntity.ok("Movie saved: " + saved.getTitle());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle-visibility")
    public ResponseEntity<String> toggleVisibility(@PathVariable long id) {
        try {
            movieService.changeVisibility(id);
            return ResponseEntity.ok("Movie visibility toggled successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteScreening(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok("Movie deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }
}
