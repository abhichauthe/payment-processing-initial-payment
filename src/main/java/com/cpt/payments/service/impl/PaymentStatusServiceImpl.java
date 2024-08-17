package com.cpt.payments.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.service.PaymentStatusService;
import com.cpt.payments.service.TransactionStatusHandler;
import com.cpt.payments.service.factory.TransactionStatusFactory;

@Service
public class PaymentStatusServiceImpl implements PaymentStatusService {

	@Autowired
	TransactionStatusFactory transactionStatusFactory;

	@Override
	public Transaction updatePaymentStatus(Transaction transaction) {
		// TODO Auto-generated method stub
		System.out.println("Invoking service class: updatePaymentStatus");

		TransactionStatusEnum statusEnum = TransactionStatusEnum.getTransactionStatusEnum(
				transaction.getTxnStatusId());

		//Factory
		TransactionStatusHandler statusHandler = transactionStatusFactory.getStatusHandler(statusEnum);

		if(statusHandler == null) {
			System.out.println(" invalid transaction handler -> " + transaction.getTxnStatusId());

			throw new PaymentProcessingException(HttpStatus.INTERNAL_SERVER_ERROR,
					ErrorCodeEnum.TRANSACTION_STATUS_HANDLER_NOT_FOUND.getErrorCode(),
					ErrorCodeEnum.TRANSACTION_STATUS_HANDLER_NOT_FOUND.getErrorMessage());
		}

		boolean isUpdated = statusHandler.updateStatus(transaction);

		System.out.println("Txn Status isUpdated:" + isUpdated + "|statusEnum:" + statusEnum);

		if(!isUpdated) {
			System.out.println("FAILED to updated transaction");

			//exit condition
			throw new PaymentProcessingException(HttpStatus.INTERNAL_SERVER_ERROR,
					ErrorCodeEnum.TRANSACTION_STATUS_UPDATE_FAILED.getErrorCode(),
					ErrorCodeEnum.TRANSACTION_STATUS_UPDATE_FAILED.getErrorMessage());
		}

		System.out.println("Trasaction updated successfully");
		return transaction;
	}

}
