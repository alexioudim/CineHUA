package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.request.ScreeningRequest;
import gr.dit.hua.CineHua.entity.Screening;
import gr.dit.hua.CineHua.service.AuditoriumService;
import gr.dit.hua.CineHua.service.MovieService;
import gr.dit.hua.CineHua.service.ScreeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/screening")
public class ScreeningController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private AuditoriumService auditoriumService;
    @Autowired
    private ScreeningService screeningService;


    public ResponseEntity<Screening> createScreening(ScreeningRequest screeningDTO){
        Screening screening = new Screening();
        screening.setMovie();
        screening.setAuditorium();
        screening.setStartTime(screeningDTO.getStartTime());
        screening.setEndTime(screeningDTO.getEndTime());
        screening.setDate(screeningDTO.getDate());

    }
}
