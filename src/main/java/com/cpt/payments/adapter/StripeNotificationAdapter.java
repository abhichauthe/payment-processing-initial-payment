package com.cpt.payments.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.pojo.stripe.StripeNotificationRequest;
import com.cpt.payments.service.PaymentStatusService;
import com.cpt.payments.utils.LogMessage;
import com.google.gson.Gson;

@Component
public class StripeNotificationAdapter {

	private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";

	private static final Logger LOGGER = LogManager.getLogger(StripeNotificationAdapter.class);

	@Autowired
	private PaymentStatusService paymentStatusServiceImpl;

	@Autowired
	private TransactionDao transactionDaoImpl;

	@Autowired
	private Gson gson;

	public void processNotification(String notificationData) {

		LogMessage.log(LOGGER, ":: converting request to stripe object ::");

		StripeNotificationRequest stripeRequest = gson.fromJson(notificationData, StripeNotificationRequest.class);

		LogMessage.log(LOGGER, ":: stripe notification object is :: " + stripeRequest);

		if (CHECKOUT_SESSION_COMPLETED.equals(stripeRequest.getType())) {

			Transaction transaction = transactionDaoImpl
					.getTransactionByProviderReference(stripeRequest.getData().getObject().getId());
			
			/*
			 * CREATED(1,"CREATED"), PENDING(2,"PENDING"), APPROVED(3,"APPROVED"),
			 * FAILED(4,"FAILED");
			 */

			
			if (null == transaction 
					|| (transaction.getTxnStatusId() == 3 
					|| transaction.getTxnStatusId() == 4 )) {
				LogMessage.log(LOGGER, "transaction not found -> " + transaction);
				throw new PaymentProcessingException(HttpStatus.BAD_REQUEST,
						ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorCode(),
						ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorMessage());
			}
			

			if ("complete".equalsIgnoreCase(stripeRequest.getData().getObject().getStatus())
					&& "paid".equalsIgnoreCase(stripeRequest.getData().getObject().getPayment_status())) {

				LogMessage.log(LOGGER, ":: stripe payment is success :: ");
				transaction.setTxnStatusId(TransactionStatusEnum.APPROVED.getId());
			}

			/*
			 * if
			 * ("complete".equalsIgnoreCase(stripeRequest.getData().getObject().getStatus())
			 * && "unpaid".equalsIgnoreCase(stripeRequest.getData().getObject().
			 * getPayment_status())) {
			 * 
			 * LogMessage.log(LOGGER, ":: stripe payment is success :: ");
			 * transaction.setTxnStatusId(TransactionStatusEnum.FAILED.getId()); }
			 */

			paymentStatusServiceImpl.updatePaymentStatus(transaction);
		}

	}

}
