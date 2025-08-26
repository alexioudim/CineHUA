package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.BookingDTO;
import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.request.TicketRequest;
import gr.dit.hua.CineHua.dto.response.BookingResponse;
import gr.dit.hua.CineHua.dto.response.BookingValidationResponse;
import gr.dit.hua.CineHua.dto.response.CreditNoteResponse;
import gr.dit.hua.CineHua.dto.response.RefundResponse;
import gr.dit.hua.CineHua.entity.Booking;
import gr.dit.hua.CineHua.entity.CreditNote;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import gr.dit.hua.CineHua.entity.Ticket;
import gr.dit.hua.CineHua.service.BookingService;
import gr.dit.hua.CineHua.service.StripeService;
import gr.dit.hua.CineHua.service.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final StripeService stripeService;

    public BookingController(BookingService bookingService, StripeService stripeService) {
        this.bookingService = bookingService;
        this.stripeService = stripeService;
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
    public ResponseEntity<RefundResponse> cancelAndRefund(@PathVariable String bookingCode,
                                                          @AuthenticationPrincipal UserDetailsImpl user) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            var resp = stripeService.cancelTicketsAndRefund(bookingCode, user.getId());
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate/{bookingCode}")
    public ResponseEntity<BookingValidationResponse> validateAndCollect(@PathVariable String bookingCode) {
        BookingValidationResponse resp = bookingService.validateAndCollect(bookingCode);
        return switch (resp.getStatus()) {
            case INVALID_NOT_FOUND -> ResponseEntity.notFound().build();
            case INVALID_PAYMENT, INVALID_ALREADY_COLLECTED, INVALID_PARTIALLY_USED, INVALID_TIME_WINDOW ->
                    ResponseEntity.badRequest().body(resp);
            default -> ResponseEntity.ok(resp);
        };
    }

    @GetMapping("/details")
    public ResponseEntity<Page<BookingDTO>> getBookingsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "issueDate,desc") String sort) {

        // Parse sort, π.χ. "issueDate,desc"
        String[] parts = sort.split(",");
        String prop = parts.length > 0 ? parts[0] : "issueDate";
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort s = Sort.by(dir, prop);

        Page<BookingDTO> result = bookingService.getBookingsPage(page, size, s);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<BookingDTO> getByCode(@PathVariable String code) {
        Optional<BookingDTO> dto = bookingService.getBookingByCode(code);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PostMapping("/cancel/{bookingCode}")
//    public CreditNoteResponse cancelBooking (@PathVariable String bookingCode, @RequestParam long user_id) {
//        CreditNote creditNote = bookingService.cancelTickets(bookingCode, user_id);
//        return new CreditNoteResponse(creditNote.getCode(), creditNote.getBalance(), creditNote.getIssueDate(), creditNote.getExpirationDate(), creditNote.getQrCode());
//    }

    @GetMapping("/byPi/{paymentIntentId}")
    public ResponseEntity<BookingResponse> getByPi(@PathVariable String paymentIntentId) {
        BookingResponse resp = bookingService.getByPaymentIntent(paymentIntentId);
        return (resp == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(resp);
    }
}
