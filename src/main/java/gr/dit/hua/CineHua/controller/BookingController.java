package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.request.TicketRequest;
import gr.dit.hua.CineHua.dto.response.BookingResponse;
import gr.dit.hua.CineHua.dto.response.CreditNoteResponse;
import gr.dit.hua.CineHua.entity.Booking;
import gr.dit.hua.CineHua.entity.CreditNote;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import gr.dit.hua.CineHua.entity.Ticket;
import gr.dit.hua.CineHua.service.BookingService;
import gr.dit.hua.CineHua.service.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/new")
    public BookingResponse createBooking (@RequestBody BookingRequest bookingRequest, @AuthenticationPrincipal UserDetailsImpl user) throws IOException {

        long userId = user.getId();
        Booking booking = bookingService.createBookingFromCart(bookingRequest, userId);

        return new BookingResponse(booking.getIssueDate(), booking.getTotalPrice(), booking.getBookingCode(), booking.getQrCode());
    }

    @GetMapping("/{bookingCode}/tickets")
    public List<TicketRequest> getAllTickets (@PathVariable String bookingCode) {
            return bookingService.getTicketsByBooking(bookingCode);
    }

    @PostMapping("/cancel/{bookingCode}")
    public CreditNoteResponse cancelBooking (@PathVariable String bookingCode, @RequestParam long user_id) {
        CreditNote creditNote = bookingService.cancelTickets(bookingCode, user_id);
        return new CreditNoteResponse(creditNote.getCode(), creditNote.getBalance(), creditNote.getIssueDate(), creditNote.getExpirationDate(), creditNote.getQrCode());
    }

    @GetMapping("/byPi/{paymentIntentId}")
    public ResponseEntity<BookingResponse> getByPi(@PathVariable String paymentIntentId) {
        BookingResponse resp = bookingService.getByPaymentIntent(paymentIntentId);
        return (resp == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(resp);
    }
}
