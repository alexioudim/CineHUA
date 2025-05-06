package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.dto.request.BookingRequest;
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

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;


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
    public List<Ticket> getTicketsByBooking (String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        return booking.getTickets();
    }

    public String generateBookingCode() {
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

}
