package com.stock.ticker.controller;

import com.stock.ticker.model.StockPriceUpdate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@RestController
 // Connects to Vite/React port securely
public class EmittedTickerController {

    private final Random random = new Random();

    @GetMapping(value = "v2/api/stocks/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<StockPriceUpdate>>> streamMockEvents() {
        
        System.out.println("🟢 New Client Connected! Setting up SSE stream pipeline.");

        return Flux.interval(Duration.ofSeconds(1)) // 1. Emit a continuous pulse every 1 second
                .map(tick -> {
                    // 2. Generate a fresh in-memory pricing array on each pulse
                    double aaplPrice = 150.0 + (random.nextDouble() - 0.5) * 5;
                    double msftPrice = 300.0 + (random.nextDouble() - 0.5) * 8;

                    return List.of(
                            new StockPriceUpdate("AAPL", Math.round(aaplPrice * 100.0) / 100.0),
                            new StockPriceUpdate("MSFT", Math.round(msftPrice * 100.0) / 100.0)
                    );
                })
                // 3. Wrap your array inside the SSE stream envelope
                .map(dataFrame -> ServerSentEvent.<List<StockPriceUpdate>>builder()
                        .id(String.valueOf(System.currentTimeMillis())) // Unique event tag
                        .event("stock-pulse")                           // Event type identifier
                        .data(dataFrame)                                // The actual body payload
                        .build())
                // 4. Lifecycle Hook: Runs the instant the browser tab is shut down
                .doOnCancel(() -> {
                    System.out.println("❌ Client closed connection. Cleaning up pipeline threads gracefully.");
                });
    }
}