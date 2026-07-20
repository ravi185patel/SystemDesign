package com.stock.ticker.controller;

import com.stock.ticker.model.Stock;
import com.stock.ticker.service.ClusterStreamHub;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController

public class InstantPortfolioController {

    private final ClusterStreamHub clusterHub;

    public InstantPortfolioController(ClusterStreamHub clusterHub) {
        this.clusterHub = clusterHub;
    }

    @GetMapping(value = "api/stocks/portfolio", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Stock>> streamInstantPortfolio(@RequestParam List<String> tickers) {

        // 💡 Generate a quick dummy frame so the connection has instant data activity
        ServerSentEvent<Stock> initFrame = ServerSentEvent.<Stock>builder()
                .comment("connection-established")
                .build();

        return Flux.concat(
                Mono.just(initFrame),
                clusterHub.getGlobalStream()
                        .filter(stock -> tickers.contains(stock.getTicker()))
                        .map(stock -> ServerSentEvent.<Stock>builder()
                                .event("portfolio-update")
                                .data(stock)
                                .build())
        );

        /*// Listen to the live global event engine
        return clusterHub.getGlobalStream()
                // 💡 Filter instantly: Only push if the changed stock matches this user's portfolio array
                .filter(stock -> tickers.contains(stock.getTicker()))
                .map(stock -> ServerSentEvent.<Stock>builder()
                        .event("portfolio-update")
                        .data(stock) // Sends just the one single updated object frame!
                        .build());*/
    }
}