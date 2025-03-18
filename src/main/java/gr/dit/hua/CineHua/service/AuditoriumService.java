package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.entity.Seat;
import gr.dit.hua.CineHua.repository.AuditoriumRepository;
import gr.dit.hua.CineHua.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriumService {

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public void createAuditorium(Auditorium auditorium) {
        int rows = auditorium.getRows();
        int columns = auditorium.getColumns();

        int capacity = rows * columns;
        auditorium.setCapacity(capacity);

        auditoriumRepository.save(auditorium);

        List<Seat> seats = new ArrayList<>();

        char row_name = 'A';
        for (int row = 0 ; row < rows ; row++) {
            for (int column = 0; column < columns; column++) {
                Seat seat = new Seat();
                seat.setRow(row_name);
                seat.setColumn(column+1);
                seat.setAuditorium(auditorium);
                seats.add(seat);

            }
            row_name += 1;
        }

        seatRepository.saveAll(seats);

        auditorium.setSeats(seats);
    }




}
