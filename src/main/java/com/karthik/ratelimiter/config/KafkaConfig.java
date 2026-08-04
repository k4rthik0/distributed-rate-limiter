package com.karthik.ratelimiter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String QUOTA_UPDATES_TOPIC = "tenant-quota-updates";

    @Bean
    public NewTopic quotaUpdatesTopic() {
        return TopicBuilder.name(QUOTA_UPDATES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
