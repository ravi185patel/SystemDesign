package com.stock.ticker.controller;

import com.stock.ticker.model.Stock;

import com.stock.ticker.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    /**
     * POST /api/stocks
     * Body: { "ticker": "NVDA", "price": 920.50 }
     *
     * Adds a new stock or updates an existing ticker price reactively.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Stock> addOrUpdateStock(@RequestBody Stock stockPayload) {
        return stockService.addOrUpdateStock(stockPayload);
    }
}
