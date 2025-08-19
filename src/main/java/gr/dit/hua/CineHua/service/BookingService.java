package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.dto.BookingDTO;
import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.request.TicketRequest;
import gr.dit.hua.CineHua.dto.response.BookingResponse;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.live.SeatLivePublisher;
import gr.dit.hua.CineHua.repository.*;
import jakarta.transaction.Transactional;
import net.glxn.qrgen.core.image.ImageType;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import net.glxn.qrgen.javase.QRCode;

@Service
public class BookingService {

    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final BookingRepository bookingRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final TicketRepository ticketRepository;
    private final SeatLivePublisher livePublisher;

    public BookingService(SeatAvailabilityRepository seatAvailabilityRepository, BookingRepository bookingRepository, TicketRepository ticketRepository, CreditNoteRepository creditNoteRepository, SeatLivePublisher livePublisher) {
        this.seatAvailabilityRepository = seatAvailabilityRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.livePublisher = livePublisher;
    }

    @Transactional
    public Booking createBookingFromCart(BookingRequest bookingRequest, long userId) throws IOException {
        return createBookingInternal(bookingRequest, userId, null, PaymentStatus.PENDING);
    }

    /**
     * POS flow (webhook): πληρωμή έχει πετύχει.
     */
    @Transactional
    public Booking createBookingFromCart(BookingRequest bookingRequest, long userId, String paymentIntentId) throws IOException {
        return createBookingInternal(bookingRequest, userId, paymentIntentId, PaymentStatus.SUCCEEDED);
    }

    // === Internal υλοποίηση ===
    @Transactional
    protected Booking createBookingInternal(BookingRequest bookingRequest,
                                            long userId,
                                            String paymentIntentId,
                                            PaymentStatus status) throws IOException {

        // 1) Φέρε και κλείδωσε θέσεις
        List<SeatAvailability> availabilities = seatAvailabilityRepository.findAllById(bookingRequest.getSeatAvailabilitiyIdList());
        for (SeatAvailability sa : availabilities) {
            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException(sa.getSeat().getColumn() + sa.getSeat().getRow() + " is not available.");
            }
            sa.setAvailability(AvailabilityStatus.SOLD);
            Long screeningId = sa.getScreening().getId();
            livePublisher.publishSold(screeningId, sa.getId());
        }

        // 2) Tickets
        Booking booking = new Booking();
        List<Ticket> tickets = new ArrayList<>();
        for (SeatAvailability sa : availabilities) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeatAvailability(sa);
            ticket.setPrice(BigDecimal.valueOf(8)); // adjust if dynamic
            ticket.setStatus(TicketStatus.PENDING); // status εισιτηρίου (όχι πληρωμής)
            tickets.add(ticket);
        }

        booking.setIssueDate(LocalDateTime.now());
        booking.setTotalPrice(calculatePrice(tickets));
        booking.setTickets(tickets);

        // 3) Κωδικός & QR
        String bookingCode;
        do {
            bookingCode = generateCode(true);
        }
        while (bookingRepository.existsByBookingCode(bookingCode));
        booking.setBookingCode(bookingCode);

        // 4) Πληρωμή
        booking.setPaymentStatus(status);
        booking.setPaymentIntentId(paymentIntentId); // μπορεί να είναι null στο non-POS flow

        // 5) Save
        return bookingRepository.save(booking);
    }

    @Transactional
    public BookingResponse getByPaymentIntent(String paymentIntentId) {
        Booking b = bookingRepository.findByPaymentIntentId(paymentIntentId);
        if (b == null) return null;
        b.setQrCode(generateQrBase64(b.getBookingCode()));
        return new BookingResponse(b.getIssueDate(), b.getTotalPrice(), b.getBookingCode(), b.getQrCode());
    }

    @Transactional
    public List<TicketRequest> getTicketsByBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        List<Ticket> tickets = booking.getTickets();
        List<TicketRequest> ticketRequests = ticketsToDTO(tickets);
        return ticketRequests;
    }

    @Transactional
    public Page<BookingDTO> getBookingsPage(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);

        // 1) Page από ids
        Page<Long> idPage = bookingRepository.findPageIds(pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, idPage.getTotalElements());
        }

        // 2) Details για αυτά τα ids
        List<Booking> bookings = bookingRepository.findAllWithDetailsByIdIn(ids);

        // 3) Optional: διατήρηση παραγγελίας σύμφωνα με sort (issueDate desc, κ.λπ.)
        // Εδώ ταξινομούμε in-memory με βάση το sort (αν χρειάζεται).
        Comparator<Booking> cmp = Comparator.comparing(Booking::getIssueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Booking::getId);
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            if ("issueDate".equalsIgnoreCase(order.getProperty())) {
                cmp = Comparator.comparing(Booking::getIssueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Booking::getId);
                if (order.isDescending()) cmp = cmp.reversed();
            }
            // πρόσθεσε κι άλλα πεδία αν τα υποστηρίξεις
        }
        bookings.sort(cmp);

        // 4) Map σε DTO
        List<BookingDTO> dtoList = bookings.stream()
                .map(this::toBookingDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, idPage.getTotalElements());
    }

    @Transactional
    public Optional<BookingDTO> getBookingByCode(String code) {
        Booking booking = bookingRepository.findOneWithDetailsByBookingCode(code);
        if (booking == null) return Optional.empty();
        return Optional.of(toBookingDTO(booking));
    }

    private BookingDTO toBookingDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setBookingCode(b.getBookingCode());
        dto.setIssueDate(b.getIssueDate());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setPaymentStatus(b.getPaymentStatus() != null ? b.getPaymentStatus().name() : null);
        dto.setPaymentIntentId(b.getPaymentIntentId());

        List<BookingDTO.TicketDetails> ticketDtos = new ArrayList<>();
        if (b.getTickets() != null) {
            for (Ticket t : b.getTickets()) {
                BookingDTO.TicketDetails td = new BookingDTO.TicketDetails();
                td.setId(t.getId());
                td.setPrice(t.getPrice());
                td.setStatus(t.getStatus() != null ? t.getStatus().name() : null);

                SeatAvailability sa = t.getSeatAvailability();
                if (sa != null) {
                    Seat seat = sa.getSeat();
                    if (seat != null) {
                        BookingDTO.SeatInfo seatInfo = new BookingDTO.SeatInfo();
                        seatInfo.setId(seat.getId());
                        seatInfo.setRow(seat.getRow());  // char
                        seatInfo.setColumn(seat.getColumn());
                        td.setSeat(seatInfo);
                    }
                    Screening sc = sa.getScreening();
                    if (sc != null) {
                        BookingDTO.ScreeningInfo si = getScreeningInfo(sc);
                        td.setScreening(si);
                    }
                }
                ticketDtos.add(td);
            }
        }
        dto.setTickets(ticketDtos);
        return dto;
    }

    private static BookingDTO.ScreeningInfo getScreeningInfo(Screening screening) {
        BookingDTO.ScreeningInfo si = new BookingDTO.ScreeningInfo();
        si.setId(screening.getId());

        // Αν το Screening έχει LocalDateTime start/end:
        if (screening.getStartTime() != null) {
            si.setStartTime(screening.getStartTime());
            si.setDate(screening.getDate());
        }
        if (screening.getEndTime() != null) {
            si.setEndTime(screening.getEndTime());
        }

        if (screening.getMovie() != null) {
            si.setMovieTitle(screening.getMovie().getTitle());
        }
        if (screening.getAuditorium() != null) {
            si.setAuditoriumName(screening.getAuditorium().getName());
        }
        return si;
    }

//    @Transactional
//    public CreditNote cancelTickets(String bookingCode, long user_id) {
//        Booking booking = bookingRepository.findByBookingCode(bookingCode);
//        List<Ticket> tickets = ticketRepository.findTicketsByBooking(booking);
//
//        if (tickets.isEmpty()) {
//            throw new EntityNotFoundException("No tickets found.");
//        }
//
//        BigDecimal totalCost = BigDecimal.ZERO;
//        for (Ticket ticket : tickets) {
//            if (ticket.getStatus() == TicketStatus.PENDING) {
//                ticket.setStatus(TicketStatus.CANCELLED);
//                totalCost = totalCost.add(ticket.getPrice());
//
//                SeatAvailability availability = ticket.getSeatAvailability();
//                availability.setAvailability(AvailabilityStatus.AVAILABLE);
//                seatAvailabilityRepository.save(availability);
//            }
//        }
//
//        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
//            throw new IllegalStateException("No active tickets found");
//        } else {
//            CreditNote creditNote = new CreditNote();
//            creditNote.setBalance(totalCost);
//            creditNote.setIssueDate(LocalDateTime.now());
//            creditNote.setExpirationDate(creditNote.getIssueDate().plusMonths(1));
//            creditNote.setStatus(CreditNoteStatus.ACTIVE);
//            creditNote.setCode(generateCode(false));
//            String qrCode = generateQrBase64(creditNote.getCode());
//            creditNote.setQrCode(qrCode);
//            creditNoteRepository.save(creditNote);
//            return creditNote;
//        }
//
//
//    }

    private String generateCode(boolean flag) {

        String CHAR_POOL;
        int CODE_LENGTH;

        //True = Booking Code, False = Credit Note Code
        if (flag) {
            CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            CODE_LENGTH = 6;
        } else {
            CHAR_POOL = "0123456789";
            CODE_LENGTH = 12;
        }

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
            ticketRequest.setTicketId(i.getId());
            ticketRequest.setPrice(i.getPrice());
            ticketRequest.setStatus(i.getStatus());
            ticketRequests.add(ticketRequest);
        }

        return ticketRequests;
    }

    public String generateQrBase64(String text) {
        ByteArrayOutputStream stream = QRCode
                .from(text)
                .withSize(250, 250)
                .to(ImageType.PNG)
                .stream();

        byte[] qrBytes = stream.toByteArray();
        return Base64.getEncoder().encodeToString(qrBytes);
    }
}
