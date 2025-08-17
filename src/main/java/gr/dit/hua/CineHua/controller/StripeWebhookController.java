package gr.dit.hua.CineHua.controller;

import com.stripe.model.Event;
import gr.dit.hua.CineHua.service.StripeService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final StripeService posService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestBody String payload,
                                         @RequestHeader("Stripe-Signature") String sig) {
        Event event;
        try {
            event = com.stripe.net.Webhook.constructEvent(payload, sig, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        System.out.println("[WEBHOOK] type=" + event.getType()); // <-- LOG #1

        var opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) {
            System.out.println("[WEBHOOK] no data object");
            return ResponseEntity.ok("no object");
        }

        var obj = opt.get();
        if (obj instanceof com.stripe.model.PaymentIntent pi) {
            System.out.println("[WEBHOOK] pi=" + pi.getId());    // <-- LOG #2
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    try {
                        posService.onPaymentSucceeded(pi.getId());
                        System.out.println("[WEBHOOK] onPaymentSucceeded called"); // <-- LOG #3
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError().build();
                    }
                }
                case "payment_intent.payment_failed", "payment_intent.canceled" -> {
                    posService.onPaymentFailed(pi.getId());
                    System.out.println("[WEBHOOK] marked fail/canceled");
                }
                default -> System.out.println("[WEBHOOK] ignored: " + event.getType());
            }
        }
        return ResponseEntity.ok().build();
    }
}