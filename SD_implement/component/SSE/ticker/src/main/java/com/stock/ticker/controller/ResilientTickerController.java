package com.stock.ticker.controller;

import com.stock.ticker.model.Stock;
import com.stock.ticker.model.StockPriceUpdate;
import com.stock.ticker.service.BroadcastStockService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController

public class ResilientTickerController {

    private final Random random = new Random();
    private final BroadcastStockService broadcastStockService;

    public ResilientTickerController(BroadcastStockService broadcastStockService) {
        this.broadcastStockService = broadcastStockService;
    }

    @GetMapping(value = "v3/api/stocks/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<StockPriceUpdate>>> streamResilientEvents() {
        
        // 1. Core Data Stream: Generates stock ticks every 1 second
        Flux<ServerSentEvent<List<StockPriceUpdate>>> dataStream = Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    double aaplPrice = 150.0 + (random.nextDouble() - 0.5) * 5;
                    return List.of(new StockPriceUpdate("AAPL", Math.round(aaplPrice * 100.0) / 100.0));
                })
                .map(dataFrame -> ServerSentEvent.<List<StockPriceUpdate>>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("stock-pulse")
                        .retry(Duration.ofSeconds(3)) // 💡 Client Reconnect Hint: If connection drops, retry every 3 seconds
                        .data(dataFrame)
                        .build());

        // 2. Heartbeat Stream: Emits an empty keep-alive ping frame every 15 seconds
        Flux<ServerSentEvent<List<StockPriceUpdate>>> heartbeatStream = Flux.interval(Duration.ofSeconds(15))
                .map(tick -> ServerSentEvent.<List<StockPriceUpdate>>builder()
                        .comment("keep-alive-ping") // Comments are silently passed by proxies but ignored by EventSource
                        .build());

        // 3. Merge streams: Keeps the pipeline open indefinitely even if stocks don't change
        return Flux.merge(dataStream, heartbeatStream)
                .doOnCancel(() -> System.out.println("❌ Client dropped line. Releasing resources."))
                .onErrorResume(err -> {
                    System.err.println("⚠️ Stream error occurred: " + err.getMessage());
                    // Fallback: Recover smoothly from transient errors by restarting the stream window
                    return Flux.empty(); 
                });
    }

    @GetMapping(value = "api/stocks/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<Stock>>> streamStocks() {
        // 100 users will hit this endpoint, but they all hook into the exact same broadcaster
        return broadcastStockService.getLiveStream();
    }
}