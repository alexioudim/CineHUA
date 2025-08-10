package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.live.SeatSseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/seat/stream")
@RequiredArgsConstructor
public class SeatStreamController {
    private final SeatSseHub hub;

    @GetMapping(value = "/{screeningId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable long screeningId) {
        return hub.subscribe(screeningId);
    }
}
