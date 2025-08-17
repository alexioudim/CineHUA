package gr.dit.hua.CineHua.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.terminal.ConnectionToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments/terminal")
public class PaymentTerminalController {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey; // sk_test_...

    @PostConstruct
    public void init() { Stripe.apiKey = stripeSecretKey; }

    @PostMapping("/connection_token")
    public Map<String, String> connectionToken() throws StripeException {
        var tok = ConnectionToken.create(new HashMap<>());
        return Map.of("secret", tok.getSecret());
    }
}