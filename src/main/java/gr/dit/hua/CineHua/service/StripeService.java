package gr.dit.hua.CineHua.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import gr.dit.hua.CineHua.dto.request.BookingRequest;
import gr.dit.hua.CineHua.dto.response.PosStartResponse;
import gr.dit.hua.CineHua.entity.*;
import gr.dit.hua.CineHua.repository.SeatAvailabilityRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final BookingService bookingService;
    private final RedissonClient redisson;

    @Value("${stripe.secret-key}") // Βεβαιώσου ότι το property λέγεται έτσι ΠΑΝΤΟΥ
    private String stripeKey;

    @Value("${stripe.currency:EUR}") // κράτα κεφαλαία για συνέπεια
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
        log.info("Stripe initialized. Currency={}, keyPresent={}", currency, stripeKey != null && !stripeKey.isBlank());
    }

    private static final String PI_CACHE_PREFIX = "pos:attempt:";

    /** helper: ελάχιστο ποσό ανά νόμισμα σε minor units */
    private long minMinorByCurrency(String ccy) {
        ccy = ccy.toUpperCase();
        return switch (ccy) {
            case "EUR", "USD" -> 50L; // 0.50
            case "GBP" -> 30L;        // 0.30
            case "JPY" -> 50L;        // zero-decimal
            default -> 1L;
        };
    }

    @Transactional
    public PosStartResponse startPosPaymentForSeats(List<Long> seatAvailabilityIds, long userId) throws Exception {
        if (seatAvailabilityIds == null || seatAvailabilityIds.isEmpty()) {
            throw new IllegalArgumentException("No seats provided");
        }

        var seats = seatAvailabilityRepository.findAllById(seatAvailabilityIds);
        if (seats.size() != seatAvailabilityIds.size()) {
            throw new IllegalStateException("Some seats not found.");
        }
        for (var sa : seats) {
            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + sa.getId() + " not AVAILABLE.");
            }
        }

        // 8.00€ ανά θέση σε cents
        long unitPriceMinor = 800L;
        long amountCents = unitPriceMinor * seats.size();

        long min = minMinorByCurrency(currency);
        if (amountCents < min) {
            // εδώ γινόταν το error που είδες
            throw new IllegalArgumentException("Amount below minimum: " + amountCents + " < " + min + " for " + currency);
        }



        Map<String,Object> params = new HashMap<>();
        params.put("amount", amountCents);               // ΠΡΕΠΕΙ να είναι long/integer σε minor units
        params.put("currency", currency.toLowerCase());  // Stripe θέλει lowercase ISO code
        params.put("payment_method_types", List.of("card_present"));
        params.put("capture_method", "automatic");
        Map<String,String> md = new HashMap<>();
        md.put("intent", "pos_pay_then_book");
        params.put("metadata", md);

        log.info("Creating PaymentIntent: currency={}, seats={}, amountCents={}", currency, seats.size(), amountCents);
        PaymentIntent pi = PaymentIntent.create(params);

        var cache = Map.of(
                "userId", userId,
                "seatIdsCsv", seatAvailabilityIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")),
                "amountCents", amountCents
        );
        redisson.getBucket(PI_CACHE_PREFIX + pi.getId()).set(cache, 15, TimeUnit.MINUTES);

        return new PosStartResponse(pi.getClientSecret(), pi.getId());
    }

    @Transactional
    public void onPaymentSucceeded(String paymentIntentId) throws Exception {
        log.info("[POS] onPaymentSucceeded pi={}", paymentIntentId); // <-- LOG A

        var bucket = redisson.<Map<String,Object>>getBucket(PI_CACHE_PREFIX + paymentIntentId);
        Map<String,Object> cache = bucket.get();
        log.info("[POS] cache={}", cache);                           // <-- LOG B (αν είναι null, πρόβλημα TTL/cache)
        if (cache == null) {
            // Κάνε log/throw για να το δεις στα logs
            throw new IllegalStateException("No cached POS attempt for " + paymentIntentId);
        }

        long userId = ((Number)cache.get("userId")).longValue();
        String[] ids = cache.get("seatIdsCsv").toString().split(",");
        List<Long> seatIds = new ArrayList<>();
        for (String s : ids) if (!s.isBlank()) seatIds.add(Long.parseLong(s));

        // προ-έλεγχος
        var seats = seatAvailabilityRepository.findAllById(seatIds);
        for (var sa : seats) {
            if (sa.getAvailability() != AvailabilityStatus.AVAILABLE) {
                throw new IllegalStateException("Seat not AVAILABLE anymore: " + sa.getId());
            }
        }

        var req = new BookingRequest();
        req.setIssuerId(userId);
        req.setSeatAvailabilitiyIdList(seatIds);

        // εδώ ΚΑΛΕΙΣ το overload με paymentIntentId
        Booking booking = bookingService.createBookingFromCart(req, userId, paymentIntentId);

        bucket.delete(); // cleanup
    }


    public void onPaymentFailed(String paymentIntentId) {
        redisson.getBucket(PI_CACHE_PREFIX + paymentIntentId).delete();
    }
}