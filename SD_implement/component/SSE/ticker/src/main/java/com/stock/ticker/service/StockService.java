package com.stock.ticker.service;


import com.stock.ticker.model.Stock;
import com.stock.ticker.repository.StockRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
}
