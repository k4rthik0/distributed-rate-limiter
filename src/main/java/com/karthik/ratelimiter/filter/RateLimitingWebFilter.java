package com.karthik.ratelimiter.filter;

import com.karthik.ratelimiter.service.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class RateLimitingWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingWebFilter.class);

    private final RateLimiterService rateLimiterService;

    public RateLimitingWebFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Bypass management/actuator and admin tenant management endpoints
        if (path.startsWith("/actuator") || path.startsWith("/api/v1/tenants")) {
            return chain.filter(exchange);
        }

        String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "tenant_free"; // Default fallback tenant
        }

        String finalTenantId = tenantId;
        return rateLimiterService.isAllowed(finalTenantId)
                .flatMap(result -> {
                    // Populate Rate Limit Response Headers
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(result.getMaxRequests()));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(result.getResetTimeMs()));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Source", result.getSource());

                    if (result.isAllowed()) {
                        return chain.filter(exchange);
                    } else {
                        log.warn("Rate limit exceeded for tenant: {}. Source: {}", finalTenantId, result.getSource());
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        String jsonResponse = String.format(
                                "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded for tenant %s. Max allowed: %d\",\"source\":\"%s\"}",
                                finalTenantId, result.getMaxRequests(), result.getSource()
                        );

                        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                    }
                });
    }
}
