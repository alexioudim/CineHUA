package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.request.ScreeningRequest;
import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.entity.Screening;
import gr.dit.hua.CineHua.service.AuditoriumService;
import gr.dit.hua.CineHua.service.MovieService;
import gr.dit.hua.CineHua.service.ScreeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/screening")
public class ScreeningController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private AuditoriumService auditoriumService;
    @Autowired
    private ScreeningService screeningService;


    @PostMapping("/new")
    public ResponseEntity<String> createScreening(ScreeningRequest screeningDTO){
        Screening screening = new Screening();
        Movie movie = movieService.findById(screeningDTO.getMovie_id());
        Auditorium auditorium = auditoriumService.findAuditoriumById(screeningDTO.getAuditorium_id());

        // Calculate EndTime should be added

        screening.setMovie(movie);
        screening.setAuditorium(auditorium);
        screening.setStartTime(screeningDTO.getStartTime());
        screening.setEndTime(screeningDTO.getEndTime());
        screening.setDate(screeningDTO.getDate());

        try {
            screeningService.createScreening(screening);
            return ResponseEntity.ok().body("Created screening for movie :" + screening.getMovie().getTitle() + "for " + screening.getDate() + " " + screening.getStartTime());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

}
