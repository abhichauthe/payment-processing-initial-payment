package com.cpt.payments.provider.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StripeProviderResponse {
	private String paymentId;
	private String redirectUrl;
}
