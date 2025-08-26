package gr.dit.hua.CineHua.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.response.PosStartResponse;
import gr.dit.hua.CineHua.dto.response.RefundResponse;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.live.SeatLivePublisher;
import gr.dit.hua.CineHua.repository.BookingRepository;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import gr.dit.hua.CineHua.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final BookingService bookingService;
    private final RedissonClient redisson;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final SeatLivePublisher livePublisher;

    @Value("${stripe.secret-key}")
    private String stripeKey;

    /** Ticket price in euros (BigDecimal) from application.properties */
    @Value("${ticket.price}")
    private BigDecimal ticketPrice;

    private static final String CURRENCY = "eur";
    private static final long EUR_MIN_MINOR = 50L; // Stripe minimum = €0.50
    private static final String PI_CACHE_PREFIX = "pos:attempt:";

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
        log.info("Stripe initialized. EUR only. keyPresent={}", stripeKey != null && !stripeKey.isBlank());
    }

    @Transactional
    public PosStartResponse startPosPaymentForSeats(List<Long> seatAvailabilityIds, long userId) throws Exception {
        if (seatAvailabilityIds == null || seatAvailabilityIds.isEmpty()) {
            throw new IllegalArgumentException("No seats provided");
        }

        List<SeatAvailability> seats = seatAvailabilityRepository.findAllById(seatAvailabilityIds);
        if (seats.size() != seatAvailabilityIds.size()) {
            throw new IllegalStateException("Some seats not found.");
        }
        for (SeatAvailability sa : seats) {
            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + sa.getId() + " not AVAILABLE.");
            }
        }

        // ticket.price from config (euros) -> minor units (cents)
        long unitPriceMinor = toMinor(ticketPrice);
        long amountCents = unitPriceMinor * seats.size();

        if (amountCents < EUR_MIN_MINOR) {
            throw new IllegalArgumentException("Amount below Stripe minimum for EUR: " + amountCents + " < " + EUR_MIN_MINOR);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountCents);
        params.put("currency", CURRENCY);
        params.put("payment_method_types", List.of("card_present"));
        params.put("capture_method", "automatic");
        params.put("metadata", Map.of("intent", "pos_pay_then_book"));

        log.info("Creating PaymentIntent: seats={}, unitPriceMinor={}, amountCents={}", seats.size(), unitPriceMinor, amountCents);
        PaymentIntent pi = PaymentIntent.create(params);

        Map<String, Object> cache = new HashMap<>();
        cache.put("userId", userId);
        cache.put("seatIdsCsv", seatAvailabilityIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        cache.put("amountCents", amountCents);

        redisson.getBucket(PI_CACHE_PREFIX + pi.getId()).set(cache, 15, TimeUnit.MINUTES);

        return new PosStartResponse(pi.getClientSecret(), pi.getId());
    }

    @Transactional
    public void onPaymentSucceeded(String paymentIntentId) throws Exception {
        log.info("[POS] onPaymentSucceeded pi={}", paymentIntentId);

        RBucket<Map<String, Object>> bucket = redisson.getBucket(PI_CACHE_PREFIX + paymentIntentId);
        Map<String, Object> cache = bucket.get();
        log.info("[POS] cache={}", cache);
        if (cache == null) {
            throw new IllegalStateException("No cached POS attempt for " + paymentIntentId);
        }

        long userId = ((Number) cache.get("userId")).longValue();
        String[] ids = cache.get("seatIdsCsv").toString().split(",");
        List<Long> seatIds = new ArrayList<>();
        for (String s : ids) {
            if (!s.isBlank()) {
                seatIds.add(Long.parseLong(s));
            }
        }

        List<SeatAvailability> seats = seatAvailabilityRepository.findAllById(seatIds);
        for (SeatAvailability sa : seats) {
            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException("Seat not AVAILABLE anymore: " + sa.getId());
            }
        }

        BookingRequest req = new BookingRequest();
        req.setIssuerId(userId);
        req.setSeatAvailabilitiyIdList(seatIds);

        bookingService.createBookingFromCart(req, userId, paymentIntentId);
        bucket.delete();
    }

    @Transactional
    public RefundResponse cancelTicketsAndRefund(String bookingCode, long userId) throws Exception {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) throw new EntityNotFoundException("Booking not found");

        if (booking.getPaymentIntentId() == null || booking.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new IllegalStateException("Booking was not paid by card or payment pending.");
        }

        BigDecimal totalToRefund = BigDecimal.ZERO;
        List<Ticket> tickets = ticketRepository.findTicketsByBooking(booking);
        if (tickets.isEmpty()) throw new EntityNotFoundException("No tickets found.");

        for (Ticket t : tickets) {
            if (t.getStatus() == TicketStatus.PENDING) {
                totalToRefund = totalToRefund.add(t.getPrice());

                SeatAvailability sa = t.getSeatAvailability();
                sa.setAvailability(AvailabilityStatus.AVAILABLE);
                seatAvailabilityRepository.save(sa);

                Long screeningId = seatAvailabilityRepository.findScreeningIdBySeatAvailabilityId(sa.getId());
                livePublisher.publishRelease(screeningId, sa.getId());

                t.setStatus(TicketStatus.CANCELLED);
            }
        }

        if (totalToRefund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No refundable tickets found.");
        }

        long amountMinor = toMinor(totalToRefund);
        String idemKey = "refund-" + booking.getId() + "-" + System.currentTimeMillis();

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(booking.getPaymentIntentId())
                .setAmount(amountMinor)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        Refund refund = Refund.create(params, com.stripe.net.RequestOptions.builder()
                .setIdempotencyKey(idemKey)
                .build());

        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        bookingRepository.save(booking);
        ticketRepository.saveAll(tickets);

        return new RefundResponse(refund.getId(), refund.getStatus(), totalToRefund);
    }

    /** EUR -> cents */
    private long toMinor(BigDecimal amountEuro) {
        return amountEuro
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public Refund refundPaymentIntent(String paymentIntentId, String reason, String idemKey) throws Exception {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        RequestOptions opts = RequestOptions.builder()
                .setIdempotencyKey(idemKey)
                .build();

        return Refund.create(params, opts);
    }


    public void onPaymentFailed(String paymentIntentId) {
        redisson.getBucket(PI_CACHE_PREFIX + paymentIntentId).delete();
    }
}