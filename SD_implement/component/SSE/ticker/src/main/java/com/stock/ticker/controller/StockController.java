package com.stock.ticker.controller;

import com.stock.ticker.model.Stock;

import com.stock.ticker.service.StockService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173/") // Default Vite+React port
@RequestMapping("v1/api/stocks/")
public class StockController {

    public final StockService stockService;

    public StockController(StockService stockService){
        this.stockService = stockService;
    }

    @GetMapping(value = "stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<Stock>> streamStockPrices(){
        return stockService.getAllStock();
    }

}
