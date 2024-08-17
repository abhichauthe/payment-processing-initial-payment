package com.cpt.payments.service;

import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;

public interface PaymentService {

	ProcessPaymentResponse processPayment(ProcessPayment processPayment);
	
	void processGetPaymentDetails();
}
