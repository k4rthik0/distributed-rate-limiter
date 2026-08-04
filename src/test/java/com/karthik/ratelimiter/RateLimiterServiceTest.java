package com.karthik.ratelimiter;

import com.github.benmanes.caffeine.cache.Cache;
import com.karthik.ratelimiter.model.RateLimitResult;
import com.karthik.ratelimiter.model.TenantQuota;
import com.karthik.ratelimiter.service.RateLimiterService;
import com.karthik.ratelimiter.service.TenantQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RateLimiterServiceTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private RedisScript<List> rateLimiterScript;

    @Mock
    private TenantQuotaService tenantQuotaService;

    @Mock
    private Cache<String, AtomicInteger> localFallbackRateCounter;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(redisTemplate, rateLimiterScript, tenantQuotaService, localFallbackRateCounter);
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        TenantQuota quota = TenantQuota.builder()
                .tenantId("tenant_test")
                .maxRequests(10)
                .windowSizeMs(60000)
                .build();

        when(tenantQuotaService.getQuotaForTenant("tenant_test")).thenReturn(quota);
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), any(List.class)))
                .thenReturn(Flux.just(List.of(1L, 3L, 10L)));

        StepVerifier.create(rateLimiterService.isAllowed("tenant_test"))
                .expectNextMatches(result -> result.isAllowed() && result.getRemainingRequests() == 7 && "REDIS".equals(result.getSource()))
                .verifyComplete();
    }

    @Test
    void shouldBlockRequestWhenLimitExceeded() {
        TenantQuota quota = TenantQuota.builder()
                .tenantId("tenant_test")
                .maxRequests(5)
                .windowSizeMs(60000)
                .build();

        when(tenantQuotaService.getQuotaForTenant("tenant_test")).thenReturn(quota);
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), any(List.class)))
                .thenReturn(Flux.just(List.of(0L, 5L, 5L)));

        StepVerifier.create(rateLimiterService.isAllowed("tenant_test"))
                .expectNextMatches(result -> !result.isAllowed() && result.getRemainingRequests() == 0)
                .verifyComplete();
    }
}
