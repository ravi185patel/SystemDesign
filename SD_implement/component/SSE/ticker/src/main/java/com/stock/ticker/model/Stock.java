package com.stock.ticker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stocks")
public class Stock {
    @Id
    private String id;
    private String ticker;
    private Double price;

    // Getters, Setters, Constructors
    public Stock() {}
    public Stock(String ticker, Double price) { this.ticker = ticker; this.price = price; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}