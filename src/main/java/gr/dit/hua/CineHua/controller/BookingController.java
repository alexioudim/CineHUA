package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.request.TicketRequest;
import gr.dit.hua.CineHua.entity.Booking;
import gr.dit.hua.CineHua.entity.CreditNote;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import gr.dit.hua.CineHua.entity.Ticket;
import gr.dit.hua.CineHua.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    List<SeatAvailability> cart = new ArrayList();

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> createBooking (@RequestBody BookingRequest bookingRequest) {
        try {
            Booking booking = bookingService.createBookingFromCart(bookingRequest);
            return ResponseEntity.ok("Booking " + booking.getBookingCode() + " created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

    @GetMapping("/{bookingCode}/tickets")
    public List<TicketRequest> getAllTickets (@PathVariable String bookingCode) {
            return bookingService.getTicketsByBooking(bookingCode);
    }

    @PostMapping("/cancel/{bookingCode}")
    public CreditNote cancelBooking (@PathVariable String bookingCode, long user_id) {
        return bookingService.cancelTickets(bookingCode, user_id);
    }
}
