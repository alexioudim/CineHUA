package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.repository.ScreeningRepository;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import gr.dit.hua.CineHua.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScreeningService {

    @Autowired
    private ScreeningRepository screeningRepository;
    private SeatRepository seatRepository;
    @Autowired
    private SeatAvailabilityRepository seatAvailabilityRepository;

    @Transactional
    public void createScreening(Screening screening){
        Auditorium auditorium = screening.getAuditorium();
        List<Seat> seats = seatRepository.findByAuditorium(auditorium);

        List<SeatAvailability> availabilities = new ArrayList<>();

        for (Seat seat : seats) {
            SeatAvailability availability = new SeatAvailability();
            availability.setSeat(seat);
            availability.setScreening(screening);
            availability.setAvailability(AvailabilityStatus.AVAILABLE);
            availabilities.add(availability);
        }

        screeningRepository.save(screening);
        seatAvailabilityRepository.saveAll(availabilities);
    }
}
