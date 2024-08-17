package com.cpt.payments.constants;

import lombok.Getter;

public enum ErrorCodeEnum {

	GENERIC_EXCEPTION("20001","Something went wrong, please try later"),
	PAYMENT_NOT_FOUND("20002","Bad request, payment request not found"), 
	PROVIDER_NOT_FOUND("20003","Bad request, provider not found"),
	FAILED_TO_CREATE_TRANSACTION("20004","payment not created at provider"),
	TRANSACTION_STATUS_HANDLER_NOT_FOUND("20005","transaction status handler not found"),
	TRANSACTION_STATUS_UPDATE_FAILED("20006","transaction status update failed"),
	
	TP_STRIPE_ERROR("20010","Unable to process payment at Stripe");
	
	@Getter
	private String errorCode;
	@Getter
	private String errorMessage;
	
	private ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode=errorCode;
		this.errorMessage=errorMessage;
	}	
}
