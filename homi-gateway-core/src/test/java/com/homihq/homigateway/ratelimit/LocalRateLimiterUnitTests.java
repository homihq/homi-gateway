package com.homihq.homigateway.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.when;

/**
 * @author Emmanouil Gkatziouras
 */
@ExtendWith(MockitoExtension.class)
public class LocalRateLimiterUnitTests {

	private static final int DEFAULT_REPLENISH_RATE = 1;

	private static final int DEFAULT_BURST_CAPACITY = 1;

	public static final String ROUTE_ID = "routeId";

	public static final String REQUEST_ID = "id";

	public static final String[] CONFIGURATION_SERVICE_BEANS = new String[0];

	@Mock
	private ApplicationContext applicationContext;

	private LocalRateLimiter localRateLimiter;

	@BeforeEach
	public void setUp() {
		when(applicationContext.getBeanNamesForType(ConfigurationService.class))
				.thenReturn(CONFIGURATION_SERVICE_BEANS);
		localRateLimiter = new LocalRateLimiter(DEFAULT_REPLENISH_RATE,
												DEFAULT_BURST_CAPACITY);
	}

	@AfterEach
	public void tearDown() {
		Mockito.reset(applicationContext);
	}

	@Test
	public void shouldThrowWhenNotInitialized() {
		Assertions.assertThrows(IllegalStateException.class,() ->localRateLimiter.isAllowed(ROUTE_ID, REQUEST_ID));
	}

}
