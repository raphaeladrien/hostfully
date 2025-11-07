package com.hostfully.app.shared;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class BookingLockRegistry {

    private final Cache<String, ReentrantLock> lockCache =
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    public ReentrantLock getLock(String propertyId) {
        return lockCache.get(propertyId, k -> new ReentrantLock());
    }
}
