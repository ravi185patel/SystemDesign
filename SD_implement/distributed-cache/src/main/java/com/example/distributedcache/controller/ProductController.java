package com.example.distributedcache.controller;

import com.example.distributedcache.dto.ProductRequest;
import com.example.distributedcache.model.Product;
import com.example.distributedcache.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/test")
    public Map<String,String> test(){
        return Map.of(
                "application", "distributed-cache",
                "status", "running"
        );
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id){
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product getProduct(@Valid @RequestBody ProductRequest productRequest){
        return productService.createProduct(productRequest);
    }

    @PutMapping("/{id}")
    public Product getProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest productRequest){
        return productService.updateProduct(id,productRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
