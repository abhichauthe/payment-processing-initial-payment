package com.cpt.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.service.factory.ProviderHandlerFactory;

//Testclass
@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

	@Mock
	TransactionDao transactionDao;
	
	@Mock
	ProviderHandlerFactory providerHandlerFactory;
	
	@Mock
	ProviderHandler providerhandler;
	
	@InjectMocks
	PaymentServiceImpl serviceImpl;
	
	//test Method
	/**
	 * 1. Prepare the test scenario
	 * 2. Invoke your functional method
	 * 3. Set expectation what you want from the code.
	 */
	@Test
	void testProcessPaymentNullTxn() {
		
		System.out.println("Invoking mytest");
		
		//preparation
		ProcessPayment processPayment = new ProcessPayment();
		
	
		// invoking method & setting expectation
		
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, 
				() -> serviceImpl.processPayment(processPayment));
		
		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
		assertEquals(ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorCode(), ex.getErrorCode());
		assertEquals(ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorMessage(), ex.getErrorMessage());
	}
	
	
	@Test
	void testProcessPaymentNullProviderHandler() {
		
		System.out.println("Invoking mytest");
		
		//preparation
		ProcessPayment processPayment = new ProcessPayment();
		//Overriding default mock behavior
		
		Transaction txn = new Transaction();
		txn.setId(12345);
		
		when(transactionDao.getTransactionById(anyLong())).thenReturn(txn);
	
		// invoking method & setting expectation
		
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, 
				() -> serviceImpl.processPayment(processPayment));
		
		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
		assertEquals(ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorCode(), ex.getErrorCode());
		assertEquals(ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorMessage(), ex.getErrorMessage());
		
	}
	
	@Test
	void testProcessPaymentSuccess() {
		
		System.out.println("Invoking mytest");
		
		//preparation
		ProcessPayment processPayment = new ProcessPayment();
		//Overriding default mock behavior
		
		Transaction txn = new Transaction();
		txn.setId(12345);
		txn.setProviderId(2);
		
		when(transactionDao.getTransactionById(anyLong())).thenReturn(txn);
	
		when(providerHandlerFactory.getProviderHandler(anyInt())).thenReturn(providerhandler);
		
		ProcessPaymentResponse mockResponse = new ProcessPaymentResponse();
		mockResponse.setPaymentReference("p-ref");
		mockResponse.setRedirectUrl("http://redirect.com");
		
		when(providerhandler.processPayment(txn, processPayment)).thenReturn(mockResponse);
		
		
		// invoking method & setting expectation
		ProcessPaymentResponse response = assertDoesNotThrow(
				() -> serviceImpl.processPayment(processPayment));
		
		assertNotNull(response);
		
		assertEquals(mockResponse.getPaymentReference(), response.getPaymentReference());
		assertEquals(mockResponse.getRedirectUrl(), response.getRedirectUrl());
		
	}
	
}
