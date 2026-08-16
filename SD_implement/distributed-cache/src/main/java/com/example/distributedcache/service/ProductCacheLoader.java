package com.example.distributedcache.service;

import com.example.distributedcache.exception.ProductNotFoundException;
import com.example.distributedcache.model.Product;
import com.example.distributedcache.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductCacheLoader {

    private final ProductRepository productRepository;
    private final CacheService cacheService;

    public ProductCacheLoader(ProductRepository productRepository, CacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    public Product loadFromDatabaseAndCache(Long id){
        Product product = productRepository
                            .findById(id)
                            .orElseThrow(()-> new ProductNotFoundException(id));

        cacheService.putProduct(id,product);
        return product;
    }
}
