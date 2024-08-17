package com.cpt.payments.pojo.stripe;

import lombok.Data;

@Data
public class StripeCheckoutSessionObject {
	private String id;
	private String object;
	private Integer amount_subtotal;
	private Integer amount_total;
	private Integer created;
	private String currency;
	private String mode;
	private String payment_intent;
	private String payment_status;
	private String status;
	private String success_url;
}
