package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.dto.response.PosStartResponse;
import gr.dit.hua.CineHua.service.StripeService;
import gr.dit.hua.CineHua.service.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pos")
@RequiredArgsConstructor
public class PosController {

    private final StripeService posService;

    @PostMapping("/start")
    public ResponseEntity<PosStartResponse> start(@RequestBody List<Long> seatAvailabilityIds,
                                     @AuthenticationPrincipal UserDetailsImpl user) throws Exception {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (seatAvailabilityIds == null || seatAvailabilityIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PosStartResponse out = posService.startPosPaymentForSeats(seatAvailabilityIds, user.getId());
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

}