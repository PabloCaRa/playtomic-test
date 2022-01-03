package com.playtomic.tests.wallet.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletTopup {

	private String walletId;

	private String creditCardNumber;

	private BigDecimal amount;

}
