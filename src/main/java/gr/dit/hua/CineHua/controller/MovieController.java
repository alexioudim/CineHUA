package gr.dit.hua.CineHua.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.repository.MovieRepository;
import gr.dit.hua.CineHua.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
@RestController
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;

    @PostMapping("/new")
    public Movie createMovie(@RequestBody Movie newMovie) {
        return movieService.saveMovie(newMovie);
    }

    @PostMapping("/new/{imdb_id}")
    public ResponseEntity<String> createMovie(@PathVariable String imdb_id) {
        String uri = "http://www.omdbapi.com/?i=";
        String api_key = "&apikey=8657bea9";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri + imdb_id + api_key, String.class);


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Movie newMovie = objectMapper.readValue(result, Movie.class);

            movieRepository.save(newMovie);

            return ResponseEntity.ok("Movie saved successfully: " + newMovie.getTitle());

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Error processing movie: "+ e.getMessage());
        }

    }
}
