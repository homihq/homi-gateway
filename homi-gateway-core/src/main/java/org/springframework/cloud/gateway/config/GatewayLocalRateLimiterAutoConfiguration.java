package org.springframework.cloud.gateway.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.homihq.homigateway.ratelimit.LocalRateLimiter;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(GatewayAutoConfiguration.class)
@ConditionalOnClass({io.github.bucket4j.Bucket4j.class })
@AutoConfigureAfter(GatewayRedisAutoConfiguration.class)
public class GatewayLocalRateLimiterAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public RateLimiter localRateLimiter(ConfigurationService configurationService) {
        return new LocalRateLimiter(configurationService);
    }

}

