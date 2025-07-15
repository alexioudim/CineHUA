package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.request.TicketRequest;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService{

    @Autowired
    private SeatAvailabilityRepository seatAvailabilityRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ScreeningRepository screeningRepository;
    @Autowired
    private CreditNoteRepository creditNoteRepository;

    @Autowired
    private TicketRepository ticketRepository;


    @Transactional
    public Booking createBookingFromCart (BookingRequest bookingRequest) {

        User issuer = userRepository.findById(bookingRequest.getIssuerId());

        List <SeatAvailability> availabilities = seatAvailabilityRepository.findAllById(bookingRequest.getSeatAvailabilitiyIdList());

        for (SeatAvailability sa : availabilities) {

            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException(sa.getSeat().getColumn() + sa.getSeat().getRow() + " is not available.");
            }

            sa.setAvailability(AvailabilityStatus.SOLD);
        }

        Booking booking = new Booking();

        List<Ticket> tickets = new ArrayList<>();
        for (SeatAvailability sa : availabilities) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeatAvailability(sa);
            ticket.setPrice(BigDecimal.valueOf(8));
            ticket.setStatus(TicketStatus.PENDING);
            tickets.add(ticket);
        }


        booking.setIssuer(issuer);
        booking.setIssueDate(LocalDateTime.now());
        booking.setTotalPrice(calculatePrice(tickets));
        booking.setTickets(tickets);

        String code;
        do {
             code = generateBookingCode();
        } while (bookingRepository.existsByBookingCode(code));

        booking.setBookingCode(code);

        bookingRepository.save(booking);

        return booking;
    }

    @Transactional
    public List<TicketRequest> getTicketsByBooking (String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        List<Ticket> tickets = booking.getTickets();
        List<TicketRequest> ticketRequests = ticketsToDTO(tickets);
        return ticketRequests;
    }

    @Transactional
    public CreditNote cancelTickets(String bookingCode, long user_id) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        List<Ticket> tickets = ticketRepository.findTicketsByBooking(booking);

        if (tickets.isEmpty()) {
            throw new EntityNotFoundException("No tickets found.");
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.PENDING) {
                ticket.setStatus(TicketStatus.CANCELLED);
                totalCost = totalCost.add(ticket.getPrice());

                SeatAvailability availability = ticket.getSeatAvailability();
                availability.setAvailability(AvailabilityStatus.AVAILABLE);
                seatAvailabilityRepository.save(availability);
            }
        }

        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("No active tickets found");
        } else {
            CreditNote creditNote = new CreditNote(totalCost);
            creditNote.setIssuer(userRepository.findById(user_id));
            creditNoteRepository.save(creditNote);
            return creditNote;
        }


    }

    private String generateBookingCode() {

        final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final int CODE_LENGTH = 6;

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(index));
        }

        return code.toString();
    }

    public BigDecimal calculatePrice(List<Ticket> tickets) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Ticket ticket : tickets) {
            totalPrice = totalPrice.add(ticket.getPrice());
        }
        return totalPrice;
    }

    public List<TicketRequest> ticketsToDTO(List<Ticket> tickets) {
        List<TicketRequest> ticketRequests = new ArrayList<>();
        for (Ticket i : tickets) {
            TicketRequest ticketRequest = new TicketRequest();
            ticketRequest.setTicketId(i.getTicket_id());
            ticketRequest.setPrice(i.getPrice());
            ticketRequest.setStatus(i.getStatus());
            ticketRequests.add(ticketRequest);
        }

        return ticketRequests;
    }
}
