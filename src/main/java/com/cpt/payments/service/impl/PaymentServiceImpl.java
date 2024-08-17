package com.cpt.payments.service.impl;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.service.PaymentService;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.service.factory.ProviderHandlerFactory;
import com.cpt.payments.utils.LogMessage;

@Service
public class PaymentServiceImpl implements PaymentService {

	private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
	
	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	private ProviderHandlerFactory providerHandlerFactory;
	
	@Autowired
	@Qualifier("asyncPaymentDetailsExecutor")
	private Executor asyncPaymentDetailsExecutor;
	
	@Override
	public ProcessPaymentResponse processPayment(ProcessPayment processPayment) {
		LogMessage.log(LOGGER, " running processPayment at service layer with processPayment:" + processPayment);
		
		Transaction txn = transactionDao.getTransactionById(
				processPayment.getTransactionId());
		
		if(txn == null) {
			LogMessage.log(LOGGER, "transaction not found txn:" + txn + "|txnID:" + processPayment.getTransactionId());
			throw new PaymentProcessingException(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorCode(),
					ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorMessage());
		}
		
		ProviderHandler providerhandler = providerHandlerFactory.getProviderHandler(txn.getProviderId());
		
		if(providerhandler == null) {
			// throw exception
			LogMessage.log(LOGGER, "provider not found -> " + txn.getProviderId());
			throw new PaymentProcessingException(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorCode(),
					ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorMessage());
		}
		
		LogMessage.debug(LOGGER, "invoking providerhandler for payment processing providerhandler:" + providerhandler);
		
		ProcessPaymentResponse serviceResponse = 
				providerhandler.processPayment(txn, processPayment);
		
		LogMessage.log(LOGGER, "got respose from ProviderHandler|| serviceRespon:" + serviceResponse);
		
		return serviceResponse;
	}

	@Override
	public void processGetPaymentDetails() {

		try {
			List<Transaction> transactionList = transactionDao.fetchAllTransactionsForReconcilation();
			
			if(null != transactionList) {
				for(Transaction tr : transactionList) {
					asyncPaymentDetailsExecutor.execute(new Runnable() {

						@Override
						public void run() {
							ProviderHandler providerHandler = providerHandlerFactory.getProviderHandler(tr.getProviderId());
							LogMessage.log(LOGGER, "providerHandler is -> " + providerHandler);
							if (null == providerHandler) {
								LogMessage.log(LOGGER, "provider not found -> " + tr.getProviderId());
								throw new PaymentProcessingException(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorCode(),
										ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorMessage());
							}

							providerHandler.processGetPaymentDetails(tr);

						}
					});

				}
			}
		} catch (Exception e) {
			LogMessage.log(LOGGER, "exception while processing get payment details -> " + e.getMessage());
			LogMessage.logException(LOGGER, e);
		}
	}

}
