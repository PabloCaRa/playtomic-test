package com.playtomic.tests.wallet.api;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletTopup;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeServiceException;
import com.playtomic.tests.wallet.service.WalletService;

@RestController
@RequestMapping("wallet")
public class WalletController {

	@NonNull
	private WalletService walletService;

	public WalletController(@NonNull WalletService walletService) {
		this.walletService = walletService;
	}

	@GetMapping("/{id}")
	public Wallet getWalletById(@PathVariable String id) {
		Wallet wallet = null;

		try {
			wallet = walletService.getWalletById(id);
		} catch (NoSuchElementException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		return wallet;
	}

	@PostMapping("/topup")
	public Wallet topupWallet(@RequestBody WalletTopup walletTopup) {
		Wallet wallet = null;

		try {
			wallet = walletService.topupWallet(walletTopup);
		} catch (NoSuchElementException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet to top-up not found", ex);
		} catch (StripeAmountTooSmallException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Amount is too small", ex);
		} catch (StripeServiceException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error with external provider", ex);
		}

		return wallet;
	}

}
