package com.example.distributedcache.service;

import com.example.distributedcache.model.CacheResult;
import com.example.distributedcache.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    private static final String PRODUCT_KEY_PREFIX ="product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(3);
    private final RedisTemplate<String, Product> redisTemplate;
    public CacheService(RedisTemplate<String, Product> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(Long id) {
        return PRODUCT_KEY_PREFIX + id;
    }


    /*public Product getProduct(Long id){
//        String key = buildKey(id);
//        return redisTemplate.opsForValue().get(key);
        String key = buildKey(id);
        try {

            return redisTemplate
                    .opsForValue()
                    .get(key);

        } catch (DataAccessException ex) {
            log.warn("Redis GET failed for key={}. Falling back to database.",key,ex);
            return null;
        }
    }*/

    public CacheResult getProduct(Long id) {

        String key = buildKey(id);

        try {

            Product product =
                    redisTemplate
                            .opsForValue()
                            .get(key);

            if (product == null) {

                return CacheResult.miss();
            }

            return CacheResult.hit(product);

        } catch (DataAccessException ex) {

            log.warn(
                    "Redis GET failed for key={}",
                    key,
                    ex
            );

            return CacheResult.unavailable();
        }
    }


    public void putProduct(Long id, Product product) {
        String key = buildKey(id);

        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            key,
                            product,
                            CACHE_TTL
                    );

        } catch (DataAccessException ex) {
            log.warn("Redis SET failed for key={}. Continuing without cache.",key,ex);
        }
//        String key = buildKey(id);
//        redisTemplate.opsForValue().set(key,product, CACHE_TTL);
//        String key = buildKey(id);
    }

    public void deleteProduct(Long id) {
//        String key = buildKey(id);
//        redisTemplate.delete(key);
        String key = buildKey(id);

        try {

            redisTemplate.delete(key);

        } catch (DataAccessException ex) {
            log.error("Redis DELETE failed for key={}. Database operation has already completed.",key,ex);
        }
    }

    /*
    2. Why DataAccessException?
        Spring's Redis integration can surface different underlying Redis/connection exceptions.

        Instead of writing:
        catch (RedisConnectionFailureException e)

        only, we use Spring's broader data-access abstraction:
        catch (DataAccessException e)

        This gives us a reasonable boundary around Redis failures.
     */
}
