package com.example.distributedcache.service;

import com.example.distributedcache.model.Product;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {
    private static final String PRODUCT_KEY_PREFIX ="product:";

    private static final Duration CACHE_TTL = Duration.ofMinutes(3);

    private final RedisTemplate<String, Product> redisTemplate;

    public CacheService(RedisTemplate<String, Product> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(Long id) {
        return PRODUCT_KEY_PREFIX + id;
    }


    public Product getProduct(Long id){
        String key = buildKey(id);
        return redisTemplate.opsForValue().get(key);
    }


    public void putProduct(Long id, Product product) {
        String key = buildKey(id);
        redisTemplate.opsForValue().set(key,product, CACHE_TTL);
    }

    public void deleteProduct(Long id) {
        String key = buildKey(id);
        redisTemplate.delete(key);
    }
}
