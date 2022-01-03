package com.playtomic.tests.wallet.service.impl;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.math.BigDecimal;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;

/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */
@RestClientTest(StripeService.class)
public class StripeServiceTest {

	URI testUri = URI.create("http://how-would-you-test-me.localhost");

	@Autowired
	private StripeService s;

	@Autowired
	private MockRestServiceServer mockRestServiceServer;

	@BeforeEach
	public void init() {
		ReflectionTestUtils.setField(s, "chargesUri", testUri);
	}

	@Test
	public void test_exception() {
		mockRestServiceServer.expect(requestTo(testUri))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY));
		Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
			s.charge("4242 4242 4242 4242", new BigDecimal(5));
		});
	}

	@Test
	public void test_ok() throws StripeServiceException {
		mockRestServiceServer.expect(requestTo(testUri)).andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));
		s.charge("4242 4242 4242 4242", new BigDecimal(15));
	}
}