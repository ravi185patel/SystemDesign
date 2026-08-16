package com.example.distributedcache.model;

/*
4. Important problem with null
We now have:
Product cachedProduct =        cacheService.getProduct(id);

and null can mean either:
1. Redis MISS
2. Redis DOWN

For now that's okay because both paths eventually go to PostgreSQL.

But there's a difference:
1)
Redis MISS ->  Try distributed lock

versus:
Redis DOWN -> Don't waste time trying Redis lock -> PostgreSQL

So let's make that distinction explicit.
 */
public record CacheResult(Status status,Product product) {

    public enum Status {
        HIT,
        MISS,
        UNAVAILABLE
    }

    public static CacheResult hit(Product product) {
        return new CacheResult(Status.HIT,product);
    }

    public static CacheResult miss() {
        return new CacheResult(Status.MISS,null);
    }

    public static CacheResult unavailable() {
        return new CacheResult(Status.UNAVAILABLE,null);
    }
}