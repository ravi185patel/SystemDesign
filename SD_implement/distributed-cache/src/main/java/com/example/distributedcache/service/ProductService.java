package com.example.distributedcache.service;

import com.example.distributedcache.dto.ProductRequest;
import com.example.distributedcache.exception.ProductNotFoundException;
import com.example.distributedcache.model.CacheResult;
import com.example.distributedcache.model.LockHandle;
import com.example.distributedcache.model.Product;
import com.example.distributedcache.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final int MAX_RETRIES = 20;
    private static final long RETRY_DELAY_MS = 50;

    private final ProductRepository productRepository;
    private final CacheService cacheService;
    private final DistributedLockService lockService;

    public ProductService(ProductRepository productRepository, CacheService cacheService, DistributedLockService lockService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
        this.lockService = lockService;
    }

    /*public Product getProduct(Long id){
        Product cachedProduct = cacheService.getProduct(id);
        if (cachedProduct != null) {
            System.out.println("CACHE HIT: product:" + id);
            return cachedProduct;
        }
        System.out.println("CACHE MISS: product:" + id);
//        String token = lockService.tryLock(id,LOCK_TTL);
        String token = lockService.tryLock(id,LOCK_TTL);
        if(token != null){
            System.out.println("LOCK ACQUIRED: products:"+id);
            try{
                cachedProduct = cacheService.getProduct(id);
                if(cachedProduct != null){
                    System.out.println("CACHE HIT AFTER LOCK: product:"+id);
                    return cachedProduct;
                }
                System.out.println("DB QUERY: product:"+id);

                Product product = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));

                cacheService.putProduct(id,product);
                System.out.println("CACHE POPULATED: product:"+id);
                return product;
            }finally {
                lockService.unLock(id,token);
                System.out.println("LOCK RELEASED: product:"+id);
            }

        }
        System.out.println("LOCK BUYS: product:"+id);
        for(int attempt = 1;attempt<=MAX_RETRIES;attempt++){
            sleep();
            cachedProduct = cacheService.getProduct(id);
            if(cachedProduct != null){
                System.out.println("CACHE HIT AFTER WAIT: product:"+id);
                return cachedProduct;
            }
        }

        System.out.println("CACHE WAIT TIMEOUT: product:"+id);

        return productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));

//        Product product = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));
//        cacheService.putProduct(id,product);
//        return product;
    }*/

    /*
        Redis DOWN
           ↓
        tryLock()
           ↓
        Exception
           ↓
        GET /product
           ↓
        500 ❌
     */
    public Product getProduct(Long id) {

        // ==========================================
        // 1. Check Redis
        // ==========================================

        CacheResult cacheResult = cacheService.getProduct(id);

        // ==========================================
        // CACHE HIT
        // ==========================================

        if (cacheResult.status() == CacheResult.Status.HIT) {

            log.info("CACHE HIT: product:{}",id);
            return cacheResult.product();

        }

        // ==========================================
        // REDIS UNAVAILABLE
        // ==========================================

        if (cacheResult.status() == CacheResult.Status.UNAVAILABLE) {

            log.warn("Redis unavailable. Reading product:{} directly from DB.",id);

            return getFromDatabase(id);
        }

        // ==========================================
        // CACHE MISS
        // ==========================================

        log.info("CACHE MISS: product:{}",id);

        // ==========================================
        // 2. Try distributed lock
        // ==========================================

        LockHandle lock = lockService.tryAcquire(id,LOCK_TTL);

        // ==========================================
        // Redis lock unavailable
        // ==========================================

        if (lock == null) {

            /*
             * We don't know whether:
             *
             * 1. Another request owns the lock
             * 2. Redis itself is unavailable
             *
             * For now, avoid blocking the request.
             *
             * Read directly from PostgreSQL.
             */

            log.info("Redis lock unavailable/busy for product:{}; reading DB.",id);
            return getFromDatabase(id);
        }

        // ==========================================
        // 3. We own the lock
        // ==========================================

        try {
            log.info("LOCK ACQUIRED: product:{}",id);

            // ======================================
            // Double-check cache
            // ======================================

            CacheResult secondCheck = cacheService.getProduct(id);

            if (secondCheck.status() == CacheResult.Status.HIT) {

                log.info("CACHE HIT AFTER LOCK: product:{}",id);

                return secondCheck.product();
            }

            // ======================================
            // Redis became unavailable
            // ======================================

            if (secondCheck.status() == CacheResult.Status.UNAVAILABLE) {

                log.warn("Redis became unavailable after lock acquisition. Reading DB.");

                return getFromDatabase(id);
            }

            // ======================================
            // Read DB
            // ======================================

            Product product = getFromDatabase(id);

            // ======================================
            // Populate cache
            // ======================================

            cacheService.putProduct(id,product);

            log.info("CACHE POPULATED: product:{}",id);

            return product;

        } finally {

            lockService.release(lock);

            log.info("LOCK RELEASED: product:{}",id);
        }
    }

    private Product getFromDatabase(Long id) {

        log.info("DATABASE QUERY: product:{}",id);

        return productRepository
                .findById(id)
                .orElseThrow(() ->new ProductNotFoundException(id));
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


    private void sleep(){
        try{
            Thread.sleep(RETRY_DELAY_MS);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting  for cache",e);
        }
    }
}
