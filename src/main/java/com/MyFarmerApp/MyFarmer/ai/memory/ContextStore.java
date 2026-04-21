package com.MyFarmerApp.MyFarmer.ai.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MVP Context Store simulating a Redis layer.
 * Future scaling: Swap this with Spring Data Redis + @RedisHash.
 */
@Slf4j
@Service
public class ContextStore {

    // Simulates Redis TTL
    private static final long CONTEXT_TTL_MINUTES = 10;
    
    // userId -> context
    private final ConcurrentHashMap<Long, SessionContext> store = new ConcurrentHashMap<>();

    public SessionContext getContext(Long userId) {
        SessionContext context = store.get(userId);
        if (context == null) {
            return null;
        }

        // Check Expiry (TTL Logic)
        if (context.getLastAccessedAt().plusMinutes(CONTEXT_TTL_MINUTES).isBefore(LocalDateTime.now())) {
            log.info("Context expired for user {}. Wiping memory.", userId);
            store.remove(userId);
            return null;
        }

        return context;
    }

    public void saveContext(SessionContext context) {
        context.updateAccessTime();
        store.put(context.getUserId(), context);
        log.info("Context saved/updated for user {}: Intent={}", context.getUserId(), context.getLastIntent());
    }

    public void clearContext(Long userId) {
        store.remove(userId);
        log.info("Explicitly wiped context for user {}", userId);
    }
}
