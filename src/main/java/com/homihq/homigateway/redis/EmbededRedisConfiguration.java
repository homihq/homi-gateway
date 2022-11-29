package com.homihq.homigateway.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value="homi.embedded.redis",
        havingValue = "true")
public class EmbededRedisConfiguration {

    @Bean
    public EmbededRedisServer embededRedisServer() {
        return new EmbededRedisServer();
    }
}
