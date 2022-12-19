package com.homihq.homigateway.ratelimit;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import reactor.core.publisher.Mono;

@ConfigurationProperties("spring.cloud.gateway.local-rate-limiter")
public class LocalRateLimiter extends AbstractRateLimiter<LocalRateLimiter.Config>
        implements ApplicationContextAware {

    /**
     * Local Rate Limiter property name.
     */
    public static final String CONFIGURATION_PROPERTY_NAME = "local-rate-limiter";

    /**
     * Remaining Rate Limit header name.
     */
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

    /**
     * Replenish Rate Limit header name.
     */
    public static final String REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";

    /**
     * Burst Capacity header name.
     */
    public static final String BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

    /**
     * Requested Tokens header name.
     */
    public static final String REQUESTED_TOKENS_HEADER = "X-RateLimit-Requested-Tokens";

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    private Config defaultConfig;

    // configuration properties
    /**
     * Whether or not to include headers containing rate limiter information, defaults to
     * true.
     */
    private boolean includeHeaders = true;

    /**
     * The name of the header that returns number of remaining requests during the current
     * second.
     */
    private String remainingHeader = REMAINING_HEADER;

    /** The name of the header that returns the replenish rate configuration. */
    private String replenishRateHeader = REPLENISH_RATE_HEADER;

    /** The name of the header that returns the burst capacity configuration. */
    private String burstCapacityHeader = BURST_CAPACITY_HEADER;

    /** The name of the header that returns the requested tokens configuration. */
    private String requestedTokensHeader = REQUESTED_TOKENS_HEADER;

    public LocalRateLimiter(ConfigurationService configurationService) {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, configurationService);
        this.initialized.compareAndSet(false, true);
    }

    /**
     * This creates an instance with default static configuration, useful in Java DSL.
     * @param defaultReplenishRate how many tokens per second in token-bucket algorithm.
     * @param defaultBurstCapacity how many tokens the bucket can hold in token-bucket
     * algorithm.
     */
    public LocalRateLimiter(int defaultReplenishRate, int defaultBurstCapacity) {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, (ConfigurationService) null);
        this.defaultConfig = new Config().setReplenishRate(defaultReplenishRate)
                                         .setBurstCapacity(defaultBurstCapacity);
    }

    /**
     * This creates an instance with default static configuration, useful in Java DSL.
     * @param defaultReplenishRate how many tokens per second in token-bucket algorithm.
     * @param defaultBurstCapacity how many tokens the bucket can hold in token-bucket
     * algorithm.
     * @param defaultRequestedTokens how many tokens are requested per request.
     */
    public LocalRateLimiter(int defaultReplenishRate, int defaultBurstCapacity,
                            int defaultRequestedTokens) {
        this(defaultReplenishRate, defaultBurstCapacity);
        this.defaultConfig.setRequestedTokens(defaultRequestedTokens);
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public String getRemainingHeader() {
        return remainingHeader;
    }

    public void setRemainingHeader(String remainingHeader) {
        this.remainingHeader = remainingHeader;
    }

    public String getReplenishRateHeader() {
        return replenishRateHeader;
    }

    public void setReplenishRateHeader(String replenishRateHeader) {
        this.replenishRateHeader = replenishRateHeader;
    }

    public String getBurstCapacityHeader() {
        return burstCapacityHeader;
    }

    public void setBurstCapacityHeader(String burstCapacityHeader) {
        this.burstCapacityHeader = burstCapacityHeader;
    }

    public String getRequestedTokensHeader() {
        return requestedTokensHeader;
    }

    public void setRequestedTokensHeader(String requestedTokensHeader) {
        this.requestedTokensHeader = requestedTokensHeader;
    }

    /**
     * Used when setting default configuration in constructor.
     * @param context the ApplicationContext object to be used by this object
     * @throws BeansException if thrown by application context methods
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (initialized.compareAndSet(false, true)) {
            if (context.getBeanNamesForType(ConfigurationService.class).length > 0) {
                setConfigurationService(context.getBean(ConfigurationService.class));
            }
        }
    }

    private Bucket createBucket(int replenishRate, int burstCapacity) {
        Refill refill = Refill.intervally(replenishRate, Duration.ofSeconds(1));
        Bandwidth limit = Bandwidth.classic(burstCapacity, refill);
        Bucket bucket = Bucket4j.builder().addLimit(limit).build();
        return bucket;
    }

    /* for testing */ Config getDefaultConfig() {
        return defaultConfig;
    }

    /**
     * This uses a basic token bucket algorithm and relies on the bucket4j library No
     * other operations can run between fetching the count and writing the new count.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Mono<Response> isAllowed(String routeId, String id) {
        if (!this.initialized.get()) {
            throw new IllegalStateException("LocalRateLimiter is not initialized");
        }

        Config routeConfig = loadConfiguration(routeId);

        // How many requests per second do you want a user to be allowed to do?
        int replenishRate = routeConfig.getReplenishRate();

        // How much bursting do you want to allow?
        int burstCapacity = routeConfig.getBurstCapacity();

        // How many tokens are requested per request?
        int requestedTokens = routeConfig.getRequestedTokens();

        final Bucket bucket = bucketMap.computeIfAbsent(id,
                                                        (key) -> createBucket(replenishRate, burstCapacity));

        final boolean allowed = bucket.tryConsume(requestedTokens);

        Response response = new Response(allowed,
                                         getHeaders(routeConfig, bucket.getAvailableTokens()));
        return Mono.just(response);
    }

    /* for testing */ Config loadConfiguration(String routeId) {
        Config routeConfig = getConfig().getOrDefault(routeId, defaultConfig);

        if (routeConfig == null) {
            routeConfig = getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS);
        }

        if (routeConfig == null) {
            throw new IllegalArgumentException(
                    "No Configuration found for route " + routeId + " or defaultFilters");
        }
        return routeConfig;
    }

    @NotNull
    public Map<String, String> getHeaders(Config config, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        if (isIncludeHeaders()) {
            headers.put(this.remainingHeader, tokensLeft.toString());
            headers.put(this.replenishRateHeader,
                        String.valueOf(config.getReplenishRate()));
            headers.put(this.burstCapacityHeader,
                        String.valueOf(config.getBurstCapacity()));
            headers.put(this.requestedTokensHeader,
                        String.valueOf(config.getRequestedTokens()));
        }
        return headers;
    }

    @Validated
    public static class Config {

        @Min(1)
        private int replenishRate;

        @Min(1)
        private int burstCapacity = 1;

        @Min(1)
        private int requestedTokens = 1;

        public int getReplenishRate() {
            return replenishRate;
        }

        public Config setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
            return this;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public Config setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
            return this;
        }

        public int getRequestedTokens() {
            return requestedTokens;
        }

        public Config setRequestedTokens(int requestedTokens) {
            this.requestedTokens = requestedTokens;
            return this;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this).append("replenishRate", replenishRate)
                                            .append("burstCapacity", burstCapacity)
                                            .append("requestedTokens", requestedTokens).toString();

        }

    }

}