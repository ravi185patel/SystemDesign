package com.stock.ticker.service;

import com.stock.ticker.model.Stock;
import com.stock.ticker.model.StockPriceUpdate;
import com.stock.ticker.repository.StockRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
public class BroadcastStockService {

    private final Flux<ServerSentEvent<List<Stock>>> sharedStockStream;

    public BroadcastStockService(StockRepository stockRepository) {
        // Build ONE global broadcast stream
        this.sharedStockStream = Flux.interval(Duration.ofSeconds(1))
                // 1. Fetch from database EXACTLY once per second, regardless of user count
                .flatMap(tick -> stockRepository.findAll().collectList())
                .map(stocks -> ServerSentEvent.<List<Stock>>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("stock-pulse")
                        .retry(Duration.ofSeconds(3))
                        .data(stocks).build())
                // 2. TURN COLD TO HOT: Broadcast the single output payload to everyone
                .publish()
                
                // 3. LIFECYCLE MANAGEMENT: 
                // Starts polling when the 1st client connects. 
                // Keeps streaming for 100 users.
                // Automatically stops polling the DB when the last user disconnects!
                .refCount(1); 
    }

    public Flux<ServerSentEvent<List<Stock>>> getLiveStream() {
        return this.sharedStockStream;
    }
}