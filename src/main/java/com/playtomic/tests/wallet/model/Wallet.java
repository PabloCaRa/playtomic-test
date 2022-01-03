package com.playtomic.tests.wallet.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document
@Data
@Builder
public class Wallet {

	@Id
	private String id;

	private BigDecimal balance;
}
