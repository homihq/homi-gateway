package com.homihq.homigateway.ratelimit;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Gkatziouras Emmanouil
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class LocalRateLimiterTests extends BaseWebClientTests {

	private static final String DEFAULT_ROUTE = "myroute";

	private static final int REPLENISH_RATE = 10;

	private static final int BURST_CAPACITY = 2 * REPLENISH_RATE;

	private static final int REQUESTED_TOKENS = 1;

	@Autowired
	private LocalRateLimiter rateLimiter;

	@BeforeEach
	public void setUp() throws Exception {
		super.setup();
		//assumeThat("Ignore on Circle", System.getenv("CIRCLECI"), is(nullValue()));
	}

	@AfterEach
	public void tearDown() throws Exception {
		rateLimiter.setIncludeHeaders(true);
	}

	@Test
	public void localRateLimiterWorks() throws Exception {
		String id = UUID.randomUUID().toString();
		addDefaultRoute();
		checkLimitEnforced(id, REPLENISH_RATE, BURST_CAPACITY, REQUESTED_TOKENS,
				DEFAULT_ROUTE);
	}

	private void addDefaultRoute() {
		rateLimiter.getConfig().put(DEFAULT_ROUTE,
				new LocalRateLimiter.Config().setBurstCapacity(BURST_CAPACITY)
						.setReplenishRate(REPLENISH_RATE)
						.setRequestedTokens(REQUESTED_TOKENS));
	}

	@Test
	public void localRateLimiterWorksForLowRates() throws Exception {
		String id = UUID.randomUUID().toString();

		int replenishRate = 1;
		int burstCapacity = 3;
		int requestedTokens = 3;

		String routeId = "low_rate_route";
		rateLimiter.getConfig().put(routeId,
				new LocalRateLimiter.Config().setBurstCapacity(burstCapacity)
						.setReplenishRate(replenishRate)
						.setRequestedTokens(requestedTokens));

		checkLimitEnforced(id, replenishRate, burstCapacity, requestedTokens, routeId);
	}

	@Test
	public void localRateLimiterDoesNotSendHeadersIfDeactivated() throws Exception {
		String id = UUID.randomUUID().toString();
		addDefaultRoute();
		rateLimiter.setIncludeHeaders(false);

		Response response = rateLimiter.isAllowed(DEFAULT_ROUTE, id).block();
		assertThat(response.isAllowed()).isTrue();
		assertThat(response.getHeaders())
				.doesNotContainKey(LocalRateLimiter.REMAINING_HEADER);
		assertThat(response.getHeaders())
				.doesNotContainKey(LocalRateLimiter.REPLENISH_RATE_HEADER);
		assertThat(response.getHeaders())
				.doesNotContainKey(LocalRateLimiter.BURST_CAPACITY_HEADER);
		assertThat(response.getHeaders())
				.doesNotContainKey(LocalRateLimiter.REQUESTED_TOKENS_HEADER);
	}

	private void checkLimitEnforced(String id, int replenishRate, int burstCapacity,
			int requestedTokens, String routeId) throws InterruptedException {
		// Bursts work
		simulateBurst(id, replenishRate, burstCapacity, requestedTokens, routeId);

		checkLimitReached(id, burstCapacity, routeId);

		Thread.sleep(Math.max(1, requestedTokens / replenishRate) * 1000);

		// # After the burst is done, check the steady state
		checkSteadyState(id, replenishRate, routeId);
	}

	private void simulateBurst(String id, int replenishRate, int burstCapacity,
			int requestedTokens, String routeId) {
		for (int i = 0; i < burstCapacity / requestedTokens; i++) {
			Response response = rateLimiter.isAllowed(routeId, id).block();
			assertThat(response.isAllowed()).as("Burst # %s is allowed", i).isTrue();
			assertThat(response.getHeaders())
					.containsKey(LocalRateLimiter.REMAINING_HEADER);
			assertThat(response.getHeaders()).containsEntry(
					LocalRateLimiter.REPLENISH_RATE_HEADER,
					String.valueOf(replenishRate));
			assertThat(response.getHeaders()).containsEntry(
					LocalRateLimiter.BURST_CAPACITY_HEADER,
					String.valueOf(burstCapacity));
			assertThat(response.getHeaders()).containsEntry(
					LocalRateLimiter.REQUESTED_TOKENS_HEADER,
					String.valueOf(requestedTokens));
		}
	}

	private void checkLimitReached(String id, int burstCapacity, String routeId) {
		Response response = rateLimiter.isAllowed(routeId, id).block();
		if (response.isAllowed()) { // TODO: sometimes there is an off by one error
			response = rateLimiter.isAllowed(routeId, id).block();
		}
		assertThat(response.isAllowed()).as("Burst # %s is not allowed", burstCapacity)
				.isFalse();
	}

	private void checkSteadyState(String id, int replenishRate, String routeId) {
		Response response;
		for (int i = 0; i < replenishRate; i++) {
			response = rateLimiter.isAllowed(routeId, id).block();
			assertThat(response.isAllowed()).as("steady state # %s is allowed", i)
					.isTrue();
		}

		response = rateLimiter.isAllowed(routeId, id).block();
		assertThat(response.isAllowed()).as("steady state # %s is allowed", replenishRate)
				.isFalse();
	}

	@EnableAutoConfiguration(exclude = {
			org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class })
	@SpringBootConfiguration
	@Import(BaseWebClientTests.DefaultTestConfig.class)
	public static class TestConfig {

	}

}
