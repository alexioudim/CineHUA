package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
