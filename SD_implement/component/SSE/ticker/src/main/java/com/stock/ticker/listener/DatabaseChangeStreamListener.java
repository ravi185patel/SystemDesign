package com.stock.ticker.listener;

import com.stock.ticker.model.Stock;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

@Service
public class DatabaseChangeStreamListener {

    public DatabaseChangeStreamListener(
            ReactiveMongoTemplate mongoTemplate, 
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        // 💡 1. Listen natively to MongoDB's internal transaction log for the stock collection
        mongoTemplate.changeStream(Stock.class)
            .watchCollection("stocks")
            .listen()
            .map(changeEvent -> changeEvent.getBody()) // Extract the updated Stock document
            .flatMap(updatedStock -> {
                try {
                    String json = objectMapper.writeValueAsString(updatedStock);
                    System.out.println("🔥 DB Updated Instantly! Publishing to Redis: " + updatedStock.getTicker());
                    
                    // 💡 2. Publish the data change to a shared global Redis channel
                    return redisTemplate.convertAndSend("market-updates", json);
                } catch (Exception e) {
                    return Mono.empty();
                }
            })
            .subscribe(); // Runs infinitely across the cluster
    }
}