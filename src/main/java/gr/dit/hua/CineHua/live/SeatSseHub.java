package gr.dit.hua.CineHua.live;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class SeatSseHub {

    private static final long SSE_TIMEOUT_MS = 0L; // no server-side timeout
    private static final long PING_PERIOD_SEC = 15;

    private final Map<Long, CopyOnWriteArrayList<Client>> byScreening = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final class Client {
        final SseEmitter emitter;
        final ScheduledFuture<?> pingJob;
        Client(SseEmitter e, ScheduledFuture<?> p) { this.emitter = e; this.pingJob = p; }
        void stop() {
            if (pingJob != null && !pingJob.isCancelled()) pingJob.cancel(true);
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    public SseEmitter subscribe(long screeningId) {
        SseEmitter em = new SseEmitter(SSE_TIMEOUT_MS);

        // Heartbeat to keep proxies alive
        ScheduledFuture<?> ping = scheduler.scheduleAtFixedRate(() -> {
            try { em.send(SseEmitter.event().name("ping").data("1")); }
            catch (IOException | IllegalStateException ignored) { /* will be cleaned up by callbacks */ }
        }, PING_PERIOD_SEC, PING_PERIOD_SEC, TimeUnit.SECONDS);

        Client client = new Client(em, ping);
        var list = byScreening.computeIfAbsent(screeningId, k -> new CopyOnWriteArrayList<>());
        list.add(client);

        Runnable cleanup = () -> {
            client.stop();
            list.remove(client);
            // optional: drop empty lists
            if (list.isEmpty()) byScreening.remove(screeningId, list);
        };

        em.onCompletion(cleanup);
        em.onTimeout(cleanup);
        em.onError(e -> cleanup.run());

        // greet
        try { em.send(SseEmitter.event().name("hello").data("ok")); } catch (IOException ignored) {}

        return em;
    }

    public void publish(SeatEvent evt) {
        var list = byScreening.get(evt.getScreeningId());
        if (list == null || list.isEmpty()) return;

        for (Client c : list) {
            try {
                c.emitter.send(SseEmitter.event().name("seat").data(evt));
            } catch (IOException | IllegalStateException e) {
                // this emitter is dead -> cleanly remove it
                c.stop();
                list.remove(c);
            }
        }
        if (list.isEmpty()) byScreening.remove(evt.getScreeningId(), list);
    }
}