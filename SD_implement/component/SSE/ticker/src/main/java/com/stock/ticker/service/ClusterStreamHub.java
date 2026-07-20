package com.stock.ticker.service;

import com.stock.ticker.model.Stock;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class ClusterStreamHub {

    // A shared, multi-subscriber hot broadcast sink channel
    private final Sinks.Many<Stock> stockSink = Sinks.many().multicast().onBackpressureBuffer();
    private final ObjectMapper objectMapper;

    public ClusterStreamHub(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // 💡 Listen to the Redis message bus for cross-node notifications
        redisTemplate.listenTo(ChannelTopic.of("market-updates"))
            .map(ReactiveSubscription.Message::getMessage)
            .doOnNext(jsonMessage -> {
                try {
                    Stock stock = objectMapper.readValue(jsonMessage, Stock.class);
                    // Pushes the stock directly into our active memory stream instantly
                    stockSink.tryEmitNext(stock); 
                } catch (Exception e) {
                    System.err.println("Failed decoding data packet");
                }
            })
            .subscribe();
    }

    public Flux<Stock> getGlobalStream() {
        return stockSink.asFlux();
    }
}