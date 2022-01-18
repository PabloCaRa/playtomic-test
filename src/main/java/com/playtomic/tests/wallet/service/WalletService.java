package com.playtomic.tests.wallet.service;

import java.util.NoSuchElementException;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletTopup;
import com.playtomic.tests.wallet.repository.WalletRepository;

@Service
public class WalletService {

	@NonNull
	private WalletRepository walletRepository;

	@NonNull
	private StripeService stripeService;

	public WalletService(@NonNull WalletRepository walletRepository, @NonNull StripeService stripeService) {
		this.walletRepository = walletRepository;
		this.stripeService = stripeService;
	}

	public Wallet getWalletById(String id) throws NoSuchElementException {
		return walletRepository.findById(id).orElseThrow();
	}

	public Wallet topupWallet(WalletTopup walletTopup)
			throws NoSuchElementException, StripeServiceException, OptimisticLockingFailureException {
		Wallet wallet = getWalletById(walletTopup.getWalletId());

		stripeService.charge(walletTopup.getCreditCardNumber(), walletTopup.getAmount());

		wallet.setBalance(wallet.getBalance().add(walletTopup.getAmount()));

		return walletRepository.save(wallet);
	}

}
