package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.dto.ScreeningMetaDTO;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.repository.ScreeningRepository;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import gr.dit.hua.CineHua.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

    public ScreeningService(ScreeningRepository screeningRepository, SeatRepository seatRepository) {
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public void createScreening(Screening screening){
        Auditorium auditorium = screening.getAuditorium();
        List<Seat> seats = seatRepository.findByAuditorium(auditorium);

        String duration = screening.getMovie().getDuration();
        int runtime = extractMinutes(duration);
        LocalTime startTime = screening.getStartTime();
        LocalTime endTime = startTime.plusMinutes(runtime);
        screening.setEndTime(endTime);

        List<SeatAvailability> availabilities = new ArrayList<>();

        for (Seat seat : seats) {
            SeatAvailability availability = new SeatAvailability();
            availability.setSeat(seat);
            availability.setScreening(screening);
            availability.setAvailability(AvailabilityStatus.AVAILABLE);
            availabilities.add(availability);
        }

        screening.setSeatAvailabilities(availabilities);
        screeningRepository.save(screening);
    }

    private int extractMinutes(String duration){
        if (!duration.matches("\\d+\\s*min")) {
            throw new IllegalArgumentException("Invalid movie duration format: " + duration);
        }
        return Integer.parseInt(duration.replaceAll("[^0-9]", ""));
    }

    @Transactional
    public void deleteScreening(long id) {
        screeningRepository.deleteById(id);
    }

    @Transactional
    public List<SeatAvailability> getSeatAvailabilities(Long screening_id){
        Screening screening = screeningRepository.findById(screening_id).orElse(null);
        assert screening != null;
        return screening.getSeatAvailabilities();
    }

    @Transactional
    public ScreeningMetaDTO getMeta(Long id) {
        Screening s = screeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new ScreeningMetaDTO(
                s.getId(), s.getMovie().getTitle(), s.getAuditorium().getName(), s.getDate(), s.getStartTime()
        );
    }

    @Transactional
    public List<Screening> getMovieScreenings(Long movieId) {
        return new ArrayList<>(screeningRepository.findByMovieId(movieId));
    }
}
