package com.playtomic.tests.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.math.BigDecimal;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletTopup;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeRestTemplateResponseErrorHandler;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.WalletService;

@SpringBootTest
public class WalletServiceTest {

	@Autowired
	private WalletService componentToTest;

	@Autowired
	private WalletRepository walletRepository;

	@Autowired
	private StripeService stripeService;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockRestServiceServer;

	private static final String WALLET_ID = UUID.randomUUID().toString();
	private static final String CREDIT_CARD_NUMBER = "0123-4567-8901-2345";
	private static final URI STRIPE_URL = URI.create("http://how-would-you-test-me.localhost");

	private Wallet storedWallet;

	@BeforeEach
	public void setup() {
		restTemplate.setErrorHandler(new StripeRestTemplateResponseErrorHandler());
		mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
		ReflectionTestUtils.setField(stripeService, "chargesUri", STRIPE_URL);
		ReflectionTestUtils.setField(stripeService, "restTemplate", restTemplate);

		storedWallet = Wallet.builder().id(WALLET_ID).balance(BigDecimal.ZERO).build();
		walletRepository.save(storedWallet);
	}

	@Test
	public void givenValidId_whenGetWalletById_thenReturnWallet() {

		Wallet wallet = componentToTest.getWalletById(WALLET_ID);

		assertEquals(storedWallet, wallet);
	}

	@Test
	public void givenNonExistentId_whenGetWalletById_thenThrowException() {
		String id = UUID.randomUUID().toString();

		assertThrows(NoSuchElementException.class, () -> componentToTest.getWalletById(id));
	}

	@Test
	public void givenExistentWallet_whenTopup_thenReturnUpdatedWallet() {
		mockRestServiceServer.expect(requestTo(STRIPE_URL))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));

		WalletTopup walletTopup = WalletTopup.builder().walletId(WALLET_ID).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.TEN).build();

		Wallet updatedWallet = componentToTest.topupWallet(walletTopup);

		assertEquals(BigDecimal.TEN, updatedWallet.getBalance());
	}

	@Test
	public void givenNonExistentWallet_whenTopup_thenThrowsNoSuchElementException() {
		WalletTopup walletTopup = WalletTopup.builder().walletId(UUID.randomUUID().toString())
				.creditCardNumber(CREDIT_CARD_NUMBER).amount(BigDecimal.TEN).build();

		assertThrows(NoSuchElementException.class, () -> componentToTest.topupWallet(walletTopup));
	}

	@Test
	public void givenExistentWallet_whenTopupAndAmountIsLessThanMinimun_thenThrowsStripeAmountTooSmallException() {
		mockRestServiceServer.expect(requestTo(STRIPE_URL))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

		WalletTopup walletTopup = WalletTopup.builder().walletId(WALLET_ID).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.ONE).build();

		assertThrows(StripeAmountTooSmallException.class, () -> componentToTest.topupWallet(walletTopup));
	}
}
