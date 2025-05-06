package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.repository.ScreeningRepository;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import gr.dit.hua.CineHua.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScreeningService {

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private SeatRepository seatRepository;

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

    public void deleteScreening(long id) {
        screeningRepository.deleteById(id);
    }

    public List<SeatAvailability> getSeatAvailabilities(Long screening_id){
        Screening screening = screeningRepository.findById(screening_id).orElse(null);
        assert screening != null;
        return screening.getSeatAvailabilities();
    }
}
