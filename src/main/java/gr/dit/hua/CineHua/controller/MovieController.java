package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
