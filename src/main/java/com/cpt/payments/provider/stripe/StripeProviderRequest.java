package com.cpt.payments.provider.stripe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripeProviderRequest {
	private String transactionReference;
	private String currency;
	private double amount;
	private Long quantity;
	private String productDescription;
	private String successUrl;
	private String cancelUrl;

}
