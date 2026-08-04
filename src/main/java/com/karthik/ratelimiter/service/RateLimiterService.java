package com.karthik.ratelimiter.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.karthik.ratelimiter.model.RateLimitResult;
import com.karthik.ratelimiter.model.TenantQuota;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RedisScript<List> rateLimiterScript;
    private final TenantQuotaService tenantQuotaService;
    private final Cache<String, AtomicInteger> localFallbackRateCounter;

    public RateLimiterService(ReactiveStringRedisTemplate redisTemplate,
                              RedisScript<List> rateLimiterScript,
                              TenantQuotaService tenantQuotaService,
                              Cache<String, AtomicInteger> localFallbackRateCounter) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterScript = rateLimiterScript;
        this.tenantQuotaService = tenantQuotaService;
        this.localFallbackRateCounter = localFallbackRateCounter;
    }

    @CircuitBreaker(name = "redisRateLimiter", fallbackMethod = "localFallbackRateLimit")
    public Mono<RateLimitResult> isAllowed(String tenantId) {
        TenantQuota quota = tenantQuotaService.getQuotaForTenant(tenantId);
        String redisKey = "rate_limit:" + tenantId;
        long now = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        List<String> keys = Collections.singletonList(redisKey);
        List<String> args = List.of(
                String.valueOf(now),
                String.valueOf(quota.getWindowSizeMs()),
                String.valueOf(quota.getMaxRequests()),
                requestId
        );

        return redisTemplate.execute(rateLimiterScript, keys, args)
                .single()
                .map(resultList -> {
                    long isAllowedFlag = ((Number) resultList.get(0)).longValue();
                    long currentRequests = ((Number) resultList.get(1)).longValue();
                    long maxRequests = ((Number) resultList.get(2)).longValue();

                    boolean allowed = (isAllowedFlag == 1);
                    long remaining = Math.max(0, maxRequests - currentRequests);

                    return RateLimitResult.builder()
                            .allowed(allowed)
                            .currentRequests(currentRequests)
                            .maxRequests(maxRequests)
                            .remainingRequests(remaining)
                            .resetTimeMs(now + quota.getWindowSizeMs())
                            .source("REDIS")
                            .build();
                });
    }

    // Resilience4j Fallback method when Redis cluster is unavailable or timing out
    public Mono<RateLimitResult> localFallbackRateLimit(String tenantId, Throwable throwable) {
        log.warn("Redis rate limiter unavailable or circuit open. Executing local fallback for tenant {}. Cause: {}", tenantId, throwable.getMessage());

        TenantQuota quota = tenantQuotaService.getQuotaForTenant(tenantId);
        AtomicInteger counter = localFallbackRateCounter.get(tenantId, k -> new AtomicInteger(0));

        int currentCount = counter.incrementAndGet();
        boolean allowed = currentCount <= quota.getMaxRequests();
        long remaining = Math.max(0, quota.getMaxRequests() - currentCount);

        return Mono.just(RateLimitResult.builder()
                .allowed(allowed)
                .currentRequests(currentCount)
                .maxRequests(quota.getMaxRequests())
                .remainingRequests(remaining)
                .resetTimeMs(System.currentTimeMillis() + quota.getWindowSizeMs())
                .source("LOCAL_FALLBACK")
                .build());
    }
}
