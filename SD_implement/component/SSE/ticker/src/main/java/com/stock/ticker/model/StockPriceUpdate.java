package com.stock.ticker.model;

public class StockPriceUpdate {
    private String ticker;
    private Double price;
    private long timestamp;

    public StockPriceUpdate(String ticker, Double price) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTicker() { return ticker; }
    public Double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
}