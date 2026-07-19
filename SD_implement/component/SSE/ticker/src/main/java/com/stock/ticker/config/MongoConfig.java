package com.stock.ticker.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient reactiveMongoClient() {
        // Force the explicit connection path to your Docker setup with credentials
        String connectionString = "mongodb://127.0.0.1:27017/Pnstock";
        return MongoClients.create(connectionString);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        // Force the template engine to lock strictly into your stock_db schema name
        return new ReactiveMongoTemplate(reactiveMongoClient(), "Pnstock");
    }
}