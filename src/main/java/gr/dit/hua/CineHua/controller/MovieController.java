package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
@RestController
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @PostMapping("/new")
    public Movie createMovie(@RequestBody Movie newMovie) {
        return movieService.saveMovie(newMovie);
    }

    @GetMapping("/new/{imdb_id}")
    public String createMovie(@PathVariable String imdb_id) {
        String uri = "http://www.omdbapi.com/?i=";
        String api_key = "&apikey=8657bea9";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri + imdb_id + api_key, String.class);
        return result;
    }
}
