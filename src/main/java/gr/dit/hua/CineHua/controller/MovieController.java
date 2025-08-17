package gr.dit.hua.CineHua.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.repository.MovieRepository;
import gr.dit.hua.CineHua.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/view")
    public List<Movie> viewMovies() {
        return movieService.findAllMovies();
    }

    @PostMapping("/new/{imdb_id}")
    public ResponseEntity<String> createMovie(@PathVariable String imdb_id) {
        String omdbUrl = "http://www.omdbapi.com/?i=" + imdb_id + "&apikey=8657bea9";
        String tmdbApiKey = "cddbcf0abd8b1714052ad43702412558";
        String tmdbFindUrl = "https://api.themoviedb.org/3/find/" + imdb_id +
                "?api_key=" + tmdbApiKey + "&external_source=imdb_id";

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 1. Fetch OMDb data
            String omdbResponse = restTemplate.getForObject(omdbUrl, String.class);
            Movie newMovie = objectMapper.readValue(omdbResponse, Movie.class);

            // 2. Fetch poster from TMDb
            String tmdbResponse = restTemplate.getForObject(tmdbFindUrl, String.class);
            JsonNode root = objectMapper.readTree(tmdbResponse);
            JsonNode results = root.path("movie_results");

            if (results.isArray() && results.size() > 0) {
                String posterPath = results.get(0).path("poster_path").asText();
                if (!posterPath.isEmpty()) {
                    String fullPosterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;
                    newMovie.setImage_url(fullPosterUrl);
                }
            }

            // 3. Save movie
            movieService.saveMovie(newMovie);
            return ResponseEntity.ok("Movie saved: " + newMovie.getTitle());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }


//    @PostMapping("/new/{imdb_id}")
//    public ResponseEntity<String> createMovie(@PathVariable String imdb_id) {
//        String uri = "http://www.omdbapi.com/?i=";
//        String api_key = "&apikey=8657bea9";
//        RestTemplate restTemplate = new RestTemplate();
//        String result = restTemplate.getForObject(uri + imdb_id + api_key, String.class);
//
//        String api_key_poster = "629|LZ2IPjytKbeCTdoiMEbDhpjoQb0Pd7Jb075yrqA9";
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Movie newMovie = objectMapper.readValue(result, Movie.class);
//
//            movieService.saveMovie(newMovie);
//
//            return ResponseEntity.ok("Movie saved successfully: " + newMovie.getTitle());
//
//        } catch (JsonProcessingException e) {
//            return ResponseEntity.badRequest().body("Error processing movie: "+ e.getMessage());
//        }
//
//    }

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
