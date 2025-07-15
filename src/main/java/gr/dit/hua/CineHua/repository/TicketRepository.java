package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Booking;
import gr.dit.hua.CineHua.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findTicketsByBooking (Booking booking);
}
