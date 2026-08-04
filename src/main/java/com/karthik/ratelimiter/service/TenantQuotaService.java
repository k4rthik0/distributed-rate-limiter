package com.karthik.ratelimiter.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.karthik.ratelimiter.config.KafkaConfig;
import com.karthik.ratelimiter.model.QuotaUpdateEvent;
import com.karthik.ratelimiter.model.TenantQuota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantQuotaService {

    private static final Logger log = LoggerFactory.getLogger(TenantQuotaService.class);

    private final Cache<String, TenantQuota> tenantQuotaCache;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Default mock repository for initial tenant configs
    private final Map<String, TenantQuota> tenantRepository = new ConcurrentHashMap<>();

    public TenantQuotaService(Cache<String, TenantQuota> tenantQuotaCache, KafkaTemplate<String, Object> kafkaTemplate) {
        this.tenantQuotaCache = tenantQuotaCache;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void initDefaultTenants() {
        TenantQuota freeTier = TenantQuota.builder()
                .tenantId("tenant_free")
                .planTier("FREE")
                .maxRequests(10)
                .windowSizeMs(60_000) // 10 requests per 60 seconds
                .build();

        TenantQuota goldTier = TenantQuota.builder()
                .tenantId("tenant_gold")
                .planTier("GOLD")
                .maxRequests(100)
                .windowSizeMs(60_000) // 100 requests per 60 seconds
                .build();

        tenantRepository.put(freeTier.getTenantId(), freeTier);
        tenantRepository.put(goldTier.getTenantId(), goldTier);

        tenantQuotaCache.put(freeTier.getTenantId(), freeTier);
        tenantQuotaCache.put(goldTier.getTenantId(), goldTier);
    }

    public TenantQuota getQuotaForTenant(String tenantId) {
        return tenantQuotaCache.get(tenantId, id ->
            tenantRepository.getOrDefault(id, TenantQuota.builder()
                    .tenantId(id)
                    .planTier("DEFAULT")
                    .maxRequests(5)
                    .windowSizeMs(60_000)
                    .build())
        );
    }

    public TenantQuota updateTenantQuota(String tenantId, String planTier, int maxRequests, long windowSizeMs) {
        TenantQuota updatedQuota = TenantQuota.builder()
                .tenantId(tenantId)
                .planTier(planTier)
                .maxRequests(maxRequests)
                .windowSizeMs(windowSizeMs)
                .build();

        tenantRepository.put(tenantId, updatedQuota);
        tenantQuotaCache.put(tenantId, updatedQuota);

        // Broadcast update via Kafka
        QuotaUpdateEvent event = QuotaUpdateEvent.builder()
                .tenantId(tenantId)
                .planTier(planTier)
                .newMaxRequests(maxRequests)
                .newWindowSizeMs(windowSizeMs)
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(KafkaConfig.QUOTA_UPDATES_TOPIC, tenantId, event);
        log.info("Published QuotaUpdateEvent to Kafka for tenant: {}", tenantId);

        return updatedQuota;
    }

    @KafkaListener(topics = KafkaConfig.QUOTA_UPDATES_TOPIC, groupId = "rate-limiter-group")
    public void handleQuotaUpdateEvent(QuotaUpdateEvent event) {
        log.info("Received Kafka QuotaUpdateEvent: {}", event);
        TenantQuota quota = TenantQuota.builder()
                .tenantId(event.getTenantId())
                .planTier(event.getPlanTier())
                .maxRequests(event.getNewMaxRequests())
                .windowSizeMs(event.getNewWindowSizeMs())
                .build();

        tenantQuotaCache.put(event.getTenantId(), quota);
        tenantRepository.put(event.getTenantId(), quota);
    }
}
