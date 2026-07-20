package com.stock.ticker.service;

import com.stock.ticker.model.Stock;
import com.stock.ticker.repository.StockRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataHub {

    // An atomic, thread-safe memory map holding [Ticker -> Stock Object]
    private final Map<String, Stock> marketCache = new ConcurrentHashMap<>();

    public MarketDataHub(StockRepository stockRepository) {
        // One engine polling the database once per second for the absolute entire universe of stocks
        Flux.interval(Duration.ofSeconds(1))
            .flatMap(tick -> stockRepository.findAll())
            .doOnNext(stock -> marketCache.put(stock.getTicker(), stock))
            .subscribe(); // Runs infinitely in the background
    }

    // A quick, non-blocking in-memory retrieval function
    public List<Stock> getCachedStocksFor(List<String> tickers) {
        return tickers.stream()
                .map(marketCache::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}