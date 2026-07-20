package com.stock.ticker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

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

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("http://localhost:5173")); // Allow React Vite Dev Server
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}