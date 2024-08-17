package com.cpt.payments.pojo.stripe;

import lombok.Data;

@Data
public class StripeNotificationRequest {
	private String id;
	private String object;
	private String api_version;
	private Integer created;
	private StripeCheckoutSessionData data;
	private Boolean livemode;
	private Integer pending_webhooks;
	private String type;
}
