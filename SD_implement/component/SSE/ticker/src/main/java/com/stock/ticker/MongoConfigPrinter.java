package com.stock.ticker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MongoConfigPrinter implements CommandLineRunner {

    @Value("${spring.data.mongodb.uri:NOT_FOUND}")
    private String uri;

    @Override
    public void run(String... args) {
        System.out.println("Mongo URI = " + uri);
    }
}