package com.homihq.homigateway.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gkatziouras Emmanouil
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("local-rate-limiter-config")
public class LocalRateLimiterConfigTests {

	@Autowired
	private LocalRateLimiter rateLimiter;

	@Autowired
	private RouteLocator routeLocator;

	@BeforeEach
	public void init() {
		// prime routes since getRoutes() no longer blocks
		routeLocator.getRoutes().collectList().block();
	}

	@Test
	public void localRateConfiguredFromEnvironment() {
		assertFilter("local_rate_limiter_config_test", 10, 20, 1, false);
	}

	@Test
	public void localRateConfiguredFromEnvironmentMinimal() {
		assertFilter("local_rate_limiter_minimal_config_test", 2, 1, 1, false);
	}

	@Test
	public void localRateConfiguredFromJavaAPI() {
		assertFilter("custom_local_rate_limiter", 20, 40, 10, false);
	}

	@Test
	public void localRateConfiguredFromJavaAPIDirectBean() {
		assertFilter("alt_custom_local_rate_limiter", 30, 60, 20, true);
	}

	private void assertFilter(String key, int replenishRate, int burstCapacity,
			int requestedTokens, boolean useDefaultConfig) {
		LocalRateLimiter.Config config;

		if (useDefaultConfig) {
			config = rateLimiter.getDefaultConfig();
		}
		else {
			assertThat(rateLimiter.getConfig()).containsKey(key);
			config = rateLimiter.getConfig().get(key);
		}
		assertThat(config).isNotNull();
		assertThat(config.getReplenishRate()).isEqualTo(replenishRate);
		assertThat(config.getBurstCapacity()).isEqualTo(burstCapacity);
		assertThat(config.getRequestedTokens()).isEqualTo(requestedTokens);

		Route route = routeLocator.getRoutes().filter(r -> r.getId().equals(key)).next()
				.block();
		assertThat(route).isNotNull();
		assertThat(route.getFilters()).hasSize(1);
	}

	@EnableAutoConfiguration(exclude = {
			org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class })
	@SpringBootConfiguration
	public static class TestConfig {

		@Bean
		public RouteLocator testRouteLocator(RouteLocatorBuilder builder) {
			return builder.routes().route("custom_local_rate_limiter",
					r -> r.path("/custom").filters(f -> f.requestRateLimiter()
							.rateLimiter(LocalRateLimiter.class,
									rl -> rl.setBurstCapacity(40).setReplenishRate(20)
											.setRequestedTokens(10))
							.and()).uri("http://localhost"))
					.route("alt_custom_local_rate_limiter",
							r -> r.path("/custom")
									.filters(f -> f.requestRateLimiter(
											c -> c.setRateLimiter(myRateLimiter())))
									.uri("http://localhost"))
					.build();

		}

		@Bean
		public LocalRateLimiter myRateLimiter() {
			return new LocalRateLimiter(30, 60, 20);
		}

	}

}
