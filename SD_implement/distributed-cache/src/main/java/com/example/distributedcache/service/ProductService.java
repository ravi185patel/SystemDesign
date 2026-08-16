package com.example.distributedcache.service;

import com.example.distributedcache.dto.ProductRequest;
import com.example.distributedcache.exception.ProductNotFoundException;
import com.example.distributedcache.model.Product;
import com.example.distributedcache.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    private final CacheService cacheService;

    public ProductService(ProductRepository productRepository,CacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    public Product getProduct(Long id){
        Product cachedProduct = cacheService.getProduct(id);
        if (cachedProduct != null) {
            System.out.println("CACHE HIT: product:" + id);
            return cachedProduct;
        }
        System.out.println("CACHE MISS: product:" + id);
        Product product = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));
        cacheService.putProduct(id,product);
        return product;
    }

    public Product createProduct(ProductRequest request){
        Product product = new Product(
                null,
                request.name(),
                request.description(),
                request.price(),
                null,
                null
        );

        return productRepository.create(
                product
        );
    }

    public Product updateProduct(Long id,ProductRequest request){
        Product product = new Product(
                null,
                request.name(),
                request.description(),
                request.price(),
                null,
                null
        );

        boolean updated = productRepository.update(id,product);
        if(!updated){
            throw new ProductNotFoundException(id);
        }
        cacheService.deleteProduct(id);
        return getProduct(id);
    }

    public void deleteProduct(Long id){
        boolean updated = productRepository.delete(id);
        if(!updated){
            throw new ProductNotFoundException(id);
        }
        cacheService.deleteProduct(id);
    }
}
