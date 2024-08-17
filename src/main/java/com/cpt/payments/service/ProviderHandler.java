package com.cpt.payments.service;

import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;

public interface ProviderHandler {

	ProcessPaymentResponse processPayment(Transaction transaction, ProcessPayment processPayment);

	void processGetPaymentDetails(Transaction transaction);
}
