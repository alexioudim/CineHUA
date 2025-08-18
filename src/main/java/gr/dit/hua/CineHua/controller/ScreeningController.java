package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.ScreeningMetaDTO;
import gr.dit.hua.CineHua.dto.request.ScreeningRequest;
import gr.dit.hua.CineHua.dto.response.ScreeningResponse;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.service.AuditoriumService;
import gr.dit.hua.CineHua.service.MovieService;
import gr.dit.hua.CineHua.service.ScreeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/screening")
public class ScreeningController {

    private final MovieService movieService;
    private final AuditoriumService auditoriumService;
    private final ScreeningService screeningService;

    public ScreeningController(MovieService movieService, AuditoriumService auditoriumService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.auditoriumService = auditoriumService;
        this.screeningService = screeningService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> createScreening(@RequestBody ScreeningRequest screeningDTO){
        Screening screening = new Screening();
        Movie movie = movieService.findById(screeningDTO.getMovie_id());
        Auditorium auditorium = auditoriumService.findAuditoriumById(screeningDTO.getAuditorium_id());

        LocalTime startTime = LocalTime.parse(screeningDTO.getStartTime());

        screening.setMovie(movie);
        screening.setAuditorium(auditorium);
        screening.setStartTime(startTime);
        screening.setDate(screeningDTO.getDate());

        try {
            screeningService.createScreening(screening);
            return ResponseEntity.ok().body("Created screening for movie : " + screening.getMovie().getTitle() + " for " + screening.getDate() + " " + screening.getStartTime());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteScreening(@PathVariable Long id) {
        try {
            screeningService.deleteScreening(id);
            return ResponseEntity.ok("Screening deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public List<SeatAvailability> getSeatAvailability(@PathVariable Long id){
        return screeningService.getSeatAvailabilities(id);
    }

    @GetMapping("/{id}/meta")
    public ScreeningMetaDTO getMeta(@PathVariable Long id) {
        return screeningService.getMeta(id);
    }

    @GetMapping("/movie/{id}")
    public List<ScreeningResponse> getMovieScreenings(@PathVariable Long id){
        return screeningService.getMovieScreenings(id)
                .stream()
                .map(ScreeningResponse::new)
                .collect(Collectors.toList());
    }
}
