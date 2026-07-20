package com.stock.ticker.controller;

import com.stock.ticker.model.Stock;
import com.stock.ticker.service.MarketDataHub;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController

public class PortfolioStreamController {

    private final MarketDataHub marketDataHub;

    public PortfolioStreamController(MarketDataHub marketDataHub) {
        this.marketDataHub = marketDataHub;
    }

    @GetMapping(value = "v3/api/stocks/portfolio", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<Stock>>> streamUserPortfolio(@RequestParam List<String> tickers) {
        
        System.out.println("💼 New Portfolio Stream open for tickers: " + tickers);

        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    // Fetch directly from the central shared memory cache! Zero DB load.
                    List<Stock> personalizedStocks = marketDataHub.getCachedStocksFor(tickers);
                    
                    return ServerSentEvent.<List<Stock>>builder()
                            .event("portfolio-update")
                            .data(personalizedStocks)
                            .build();
                })
                .doOnCancel(() -> System.out.println("❌ Portfolio stream disconnected for: " + tickers));
    }
}