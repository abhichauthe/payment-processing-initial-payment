package com.cpt.payments.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cpt.payments.service.PaymentService;
import com.cpt.payments.utils.LogMessage;

@Component
public class PaymentStatusCheckScheduler {
	
	private static final Logger LOGGER = LogManager.getLogger(PaymentStatusCheckScheduler.class);
	
	@Autowired
	private PaymentService paymentService;
	
	//@Scheduled(cron = "${payment.status.cron:-}")
	public void paymentStatusScheduler() {
		LogMessage.log(LOGGER, ":: get payment status cron triggered ::" );
		paymentService.processGetPaymentDetails();
	}

}
