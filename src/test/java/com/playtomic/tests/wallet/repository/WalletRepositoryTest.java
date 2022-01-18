package com.playtomic.tests.wallet.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import com.playtomic.tests.wallet.model.Wallet;

@SpringBootTest
public class WalletRepositoryTest {

	@Autowired
	private WalletRepository componentToTest;

	private static final String WALLET_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setup() {
		Wallet wallet = Wallet.builder().id(WALLET_ID).balance(BigDecimal.ONE).build();
		componentToTest.save(wallet);
	}

	@Test
	public void givenExistentWallet_whenConcurrentUpdate_thenThrowsOptimisticLockingFailureException() {
		Wallet firstWallet = componentToTest.findById(WALLET_ID).get();
		Wallet secondWallet = componentToTest.findById(WALLET_ID).get();

		firstWallet.setBalance(firstWallet.getBalance().add(BigDecimal.TEN));
		componentToTest.save(firstWallet);

		secondWallet.setBalance(secondWallet.getBalance().add(BigDecimal.ONE));
		assertThrows(OptimisticLockingFailureException.class, () -> componentToTest.save(secondWallet));

	}

}
