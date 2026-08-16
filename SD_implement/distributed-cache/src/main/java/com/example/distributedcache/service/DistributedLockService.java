package com.example.distributedcache.service;

import com.example.distributedcache.model.LockHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;


/*
Cache stampede protection
Without lock = 1000 requests arrives at same time.

1) Redis MISS -> PostgreSQL
2) Redis MISS -> PostgreSQL
3) Redis MISS -> PostgreSQL
....

---->
100 req -> Redis MISS -> Request #1 gets lock -> PostgreSQL -> Redis set -> other 99 requests -> redis hit

----------- Distributed Redis Lock ---------------------
The lock
product:1 ===> lock:product:1 ( keep both separate )


 */
@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "lock:product:";
    private static final String UNLOCK_SCRIPT = """
            if redis.call('get',KEYS[1]) == ARGV[1] then
              return redis.call('del',KEYS[1])
            else
              return 0;
            end  
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> unlockScript;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT,Long.class);
    }

    public String tryLock(Long productId, Duration ttl){
        String lockKey = buildLockKey(productId);
        String token = UUID.randomUUID().toString();

        boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,token,ttl);
        if(acquired){
            return token;
        }
        return null;
    }


    public void unLock(Long productId,String token){
        String lockKey = buildLockKey(productId);
        redisTemplate.execute(unlockScript, List.of(lockKey),token);
    }

    private String buildLockKey(Long productId){
        return LOCK_PREFIX + productId;
    }

    public LockHandle tryAcquire(Long productId, Duration ttl){
        String key = LOCK_PREFIX + productId;
        String token = UUID.randomUUID().toString();
        try {
            boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
            if (!acquired) {
                return null;
            }
            return new LockHandle(key, token);

        } catch (DataAccessException ex) {

            log.warn("Unable to acquire Redis lock: {}. Cache coordination unavailable.",key,ex);
            return null;
        }

    }

    public void release(LockHandle handle) {

        if (handle == null) {
            return;
        }

        try {

            redisTemplate.execute(unlockScript,List.of(handle.key()),handle.token());

        } catch (DataAccessException ex) {

            /*
             * The request has already completed.
             *
             * Redis failure here should not turn
             * a successful API response into a failure.
             */
            log.warn(
                    "Failed to release Redis lock: {}",
                    handle.key(),
                    ex
            );
        }
    }
}
