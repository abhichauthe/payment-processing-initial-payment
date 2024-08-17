package com.cpt.payments.service.impl.provider.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.http.HttpRequest;
import com.cpt.payments.http.HttpRestTemplateEngine;
import com.cpt.payments.provider.ProviderServiceErrorResponse;
import com.cpt.payments.provider.stripe.StripeProviderRequest;
import com.cpt.payments.provider.stripe.StripeProviderResponse;
import com.cpt.payments.service.PaymentStatusService;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.utils.LogMessage;
import com.google.gson.Gson;

@Service
public class StripeProviderHandler implements ProviderHandler {

	private static final int MAX_RETRY_ATTEMPT = 3;

	private static final Logger LOGGER = LogManager.getLogger(StripeProviderHandler.class);

	@Value("${stripe.provider.service.process.payment}")
	private String processPaymentUrl;

	@Autowired
	private HttpRestTemplateEngine httpRestTemplateEngine;

	@Autowired
	private PaymentStatusService paymentStatusService;

	@Autowired
	private TransactionDao transactionDao;
	
	@Value("${stripe.provider.service.get.payment.details}")
	private String getPaymentDetailsUrl;
	
	@Value("${stripe.provider.service.get.payment.expire}")
	private String expirePaymentUrl;

	@Override
	public ProcessPaymentResponse processPayment(Transaction transaction, ProcessPayment processingServiceRequest) {
		LogMessage.debug(LOGGER, "Invoking Stripe Provider handler to call Stripe Provider service|| transaction:" + transaction+"|processingServiceRequest:" + processingServiceRequest);


		StripeProviderRequest stripeProviderRequest = StripeProviderRequest.builder()
				.transactionReference(transaction.getTxnReference())
				.currency(transaction.getCurrency())
				.amount(transaction.getAmount())
				.quantity(processingServiceRequest.getQuantity())
				.productDescription(processingServiceRequest.getProductDescription())
				.successUrl(processingServiceRequest.getSuccessUrl())
				.cancelUrl(processingServiceRequest.getCancelUrl())
				.build();

		LogMessage.log(LOGGER, " stripe provider request -> " + stripeProviderRequest);

		//Before call Stripe provider service update payment status to INitiated.
		transaction.setTxnStatusId(TransactionStatusEnum.INITATED.getId());
		transaction = paymentStatusService.updatePaymentStatus(transaction);

		Gson gson = new Gson();

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.request(gson.toJson(stripeProviderRequest))
				.url(processPaymentUrl)
				.build();

		ResponseEntity<String> response = httpRestTemplateEngine.execute(httpRequest);

		LogMessage.log(LOGGER, "Got API response from Stripe provider||response:" + response);

		boolean isRedirectUrlNull = false;

		if(response != null
				&& response.getBody() != null 
				&& response.getStatusCode().value() == HttpStatus.OK.value()) {

			StripeProviderResponse stripeProviderResponse = gson.fromJson(
					response.getBody(),
					StripeProviderResponse.class);

			if (stripeProviderResponse.getRedirectUrl() != null) {// Success
				transaction.setTxnStatusId(TransactionStatusEnum.PENDING.getId());
				transaction = paymentStatusService.updatePaymentStatus(transaction);

				transaction.setProviderReference(stripeProviderResponse.getPaymentId());
				transactionDao.updateProviderReference(transaction);

				return ProcessPaymentResponse.builder()
						.paymentReference(transaction.getTxnReference())
						.redirectUrl(stripeProviderResponse.getRedirectUrl())
						.build();
			} else { 
				LogMessage.log(LOGGER, "received null redirecturl| stripeProviderResponse:" + stripeProviderResponse);
				isRedirectUrlNull = true;
			}
		}

		throw processFailedResponse(transaction, gson, response, isRedirectUrlNull);
	}

	/**
	 * Check for type of failure.
	 * update FAILED status in DB.
	 * Updates errorCode & message in DB.
	 * throws Exception, for generating valid errorResponse
	 * 
	 * @param transaction
	 * @param gson
	 * @param response
	 * @param isRedirectUrlNull
	 */
	private PaymentProcessingException processFailedResponse(Transaction transaction, Gson gson, ResponseEntity<String> response,
			boolean isRedirectUrlNull) {
		if (null == response || null == response.getBody()) {//Failed to make API call
			LogMessage.log(LOGGER, " Payment Failed - payment creation at provider failed -> " + response);

			updateFailedPayment(transaction, 
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
			
			return new PaymentProcessingException(
					HttpStatus.INTERNAL_SERVER_ERROR, 
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
		}

		if (isRedirectUrlNull) {
			LogMessage.log(LOGGER, " Payment Failed - No Redirect URL from Stripe -> " + response);
			
			updateFailedPayment(transaction, 
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
			
			return new PaymentProcessingException(
					HttpStatus.BAD_GATEWAY, 
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
		}

		// Error response
		ProviderServiceErrorResponse errorResponse = gson.fromJson(response.getBody(),
				ProviderServiceErrorResponse.class);

		if(errorResponse.isTpProviderError()) { //ThirdParty Trustly Error
			LogMessage.log(LOGGER, " Payment Failed - is Stripe Error -> " + response);

			updateFailedPayment(transaction, 
					errorResponse.getErrorCode(),
					errorResponse.getErrorMessage());
			
			//3rdParty error needs to be mapped with our system error & returned.
			return new PaymentProcessingException(
					HttpStatus.BAD_GATEWAY, 
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
					ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
			
		} else {// Trustly Provider Service error, should be returned back to invoker
			LogMessage.log(LOGGER, " INTERNAL payment creation at provider failed -> " + response);
			
			updateFailedPayment(transaction, 
					errorResponse.getErrorCode(),
					errorResponse.getErrorMessage());
			
			return new PaymentProcessingException(HttpStatus.BAD_GATEWAY,
					errorResponse.getErrorCode(),
					errorResponse.getErrorMessage());
		}
	}

	private void updateFailedPayment(
			Transaction transaction, 
			String errorCode, String errorMessage) {

		transaction.setTxnStatusId(TransactionStatusEnum.FAILED.getId());

		transaction.setProviderCode(errorCode);
		transaction.setProviderMessage(errorMessage);

		paymentStatusService.updatePaymentStatus(transaction);
	}

	@Override
	public void processGetPaymentDetails(Transaction transaction) {
		LogMessage.log(LOGGER, " stripe provider request -> " + transaction);
		Gson gson = new Gson();
		
		if(null == transaction.getProviderReference() || transaction.getProviderReference().isBlank()) {
			LogMessage.log(LOGGER, "Failing transaction beacuse we do not have provider reference --> "+transaction.getTxnReference());
			transaction.setTxnStatusId(TransactionStatusEnum.FAILED.getId());
			paymentStatusService.updatePaymentStatus(transaction);
		}
		
		transactionDao.updateRetryCount(transaction);

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.request(gson.toJson(transaction))
				.url(getPaymentDetailsUrl)
				.build();

		LogMessage.log(LOGGER, " updating transaction status to pending ");

		ResponseEntity<String> response = httpRestTemplateEngine.execute(httpRequest);
		
		if (null == response || null == response.getBody()) {//Failed to make API call
			LogMessage.log(LOGGER, " payment creation at provider failed -> " + response);
			throw new PaymentProcessingException(HttpStatus.INTERNAL_SERVER_ERROR,
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
		}
		
		if(200 == response.getStatusCode().value()) {
			transaction = paymentStatusService.updatePaymentStatus(
					gson.fromJson(response.getBody(), Transaction.class));
		}
		
		if(transaction.getTxnStatusId().equals(TransactionStatusEnum.PENDING.getId())
				&& transaction.getRetryCount() >= MAX_RETRY_ATTEMPT) {
			// Expire API
			expireTransaction(transaction, gson);

		}
		
	}

	private void expireTransaction(Transaction transaction, Gson gson) {
		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.request(gson.toJson(transaction))
				.url(expirePaymentUrl)
				.build();

		LogMessage.log(LOGGER, " invoking /expire API providerReference:" + transaction.getProviderReference());

		ResponseEntity<String> response = httpRestTemplateEngine.execute(httpRequest);
		
		if (null == response || null == response.getBody()) {//Failed to make API call
			LogMessage.log(LOGGER, " /expire call at provider failed -> " + response);
			throw new PaymentProcessingException(HttpStatus.INTERNAL_SERVER_ERROR,
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
		}
		
		if(200 == response.getStatusCode().value()) {
			paymentStatusService.updatePaymentStatus(
					gson.fromJson(response.getBody(), Transaction.class));
		}
	}

}
