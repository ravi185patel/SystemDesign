package com.stock.ticker.service;


import com.stock.ticker.model.Stock;
import com.stock.ticker.repository.StockRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class StockService {

    public final StockRepository stockRepository;

    public StockService(StockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    public Flux<List<Stock>> getAllStock(){
        System.out.println(" printing stock tickers ");
//        stockRepository.findAll()
//                .subscribe(System.out::println);
//        return null;
        return Flux.interval(Duration.ofSeconds(1)) // Trigger a pulse every 1 second
                .flatMap(tick -> stockRepository.findAll().collectList()) // Fetch all stocks reactively
                .log(); // Optional: monitors the stream in application logs
    }

    public Mono<Stock> addOrUpdateStock(Stock stockPayload){

        System.out.println("📥 Incoming stock updates for: " + stockPayload.getTicker());

        // 1. Look up if the ticker already exists to preserve its MongoDB unique _id
        return stockRepository.findByTicker(stockPayload.getTicker())
                .flatMap(existingStock -> {
                    existingStock.setPrice(stockPayload.getPrice());
                    return stockRepository.save(existingStock);
                })
                // 2. If it does not exist, save it cleanly as a fresh document record
                .switchIfEmpty(Mono.defer(() -> {
                    return stockRepository.save(stockPayload);
                }))
                .doOnSuccess(saved -> System.out.println("💾 Document successfully committed to MongoDB: " + saved.getTicker()));
    }
}
