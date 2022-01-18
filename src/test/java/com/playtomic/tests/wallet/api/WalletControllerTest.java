package com.playtomic.tests.wallet.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletTopup;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeServiceException;
import com.playtomic.tests.wallet.service.WalletService;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WalletService walletService;

	private String walletId;

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String CREDIT_CARD_NUMBER = "0123-4567-8901-2345";

	@BeforeEach
	public void setup() {
		walletId = UUID.randomUUID().toString();
	}

	@Test
	public void givenValidId_whenGetWalletById_thenReturnWallet() throws Exception {
		Wallet wallet = Wallet.builder().id(walletId).balance(BigDecimal.ZERO).build();

		given(walletService.getWalletById(walletId)).willReturn(wallet);

		mockMvc.perform(get("/wallet/" + walletId).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("id", is(walletId))).andExpect(jsonPath("balance", is(0)));
	}

	@Test
	public void givenNonValidId_whenGetWalletById_thenReturnNotFound() throws Exception {
		given(walletService.getWalletById(walletId)).willThrow(NoSuchElementException.class);

		mockMvc.perform(get("/wallet/" + walletId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void givenValidWalletId_whenTopup_thenReturnUpdatedWallet() throws Exception {
		WalletTopup walletTopup = WalletTopup.builder().walletId(walletId).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.TEN).build();
		Wallet expectedWallet = Wallet.builder().id(walletId).balance(BigDecimal.TEN).build();

		given(walletService.topupWallet(walletTopup)).willReturn(expectedWallet);

		mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON)
				.content(MAPPER.writeValueAsBytes(walletTopup))).andExpect(status().isOk())
				.andExpect(jsonPath("id", is(walletId))).andExpect(jsonPath("balance", is(10)));
	}

	@Test
	public void givenNonExistsWalletId_whenTopup_thenReturnNotFound() throws Exception {
		WalletTopup walletTopup = WalletTopup.builder().walletId(walletId).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.TEN).build();

		given(walletService.topupWallet(walletTopup)).willThrow(NoSuchElementException.class);

		mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON)
				.content(MAPPER.writeValueAsBytes(walletTopup))).andExpect(status().isNotFound());
	}

	@Test
	public void givenValidWalletId_whenTopupAndStripeFails_thenReturnStripeServiceException() throws Exception {
		WalletTopup walletTopup = WalletTopup.builder().walletId(walletId).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.TEN).build();

		given(walletService.topupWallet(walletTopup)).willThrow(StripeServiceException.class);

		mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON)
				.content(MAPPER.writeValueAsBytes(walletTopup))).andExpect(status().isServiceUnavailable());
	}

	@Test
	public void givenValidWalletId_whenTopupAndAmountIsLessthanMinimal_thenReturnStripeAmountTooSmallException()
			throws Exception {
		WalletTopup walletTopup = WalletTopup.builder().walletId(walletId).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.ONE).build();

		given(walletService.topupWallet(walletTopup)).willThrow(StripeAmountTooSmallException.class);

		mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON)
				.content(MAPPER.writeValueAsBytes(walletTopup))).andExpect(status().isBadRequest());
	}

	@Test
	public void givenValidWalletId_whenConcurrentTopup_thenReturnOptimisticLockingFailureException() throws Exception {
		WalletTopup walletTopup = WalletTopup.builder().walletId(walletId).creditCardNumber(CREDIT_CARD_NUMBER)
				.amount(BigDecimal.ONE).build();

		given(walletService.topupWallet(walletTopup)).willThrow(OptimisticLockingFailureException.class);

		mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON)
				.content(MAPPER.writeValueAsBytes(walletTopup))).andExpect(status().isConflict());
	}

}
