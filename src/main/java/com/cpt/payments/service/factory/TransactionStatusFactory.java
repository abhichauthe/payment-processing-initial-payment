package com.cpt.payments.service.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.service.TransactionStatusHandler;
import com.cpt.payments.service.impl.status.handler.ApprovedTransactionStatusHandler;
import com.cpt.payments.service.impl.status.handler.CreatedTransactionStatusHandler;
import com.cpt.payments.service.impl.status.handler.FailedTransactionStatusHandler;
import com.cpt.payments.service.impl.status.handler.InitiatedTransactionStatusHandler;
import com.cpt.payments.service.impl.status.handler.PendingTransactionStatusHandler;

@Component
public class TransactionStatusFactory {
	
	@Autowired
	private ApplicationContext context;

	public TransactionStatusHandler getStatusHandler(TransactionStatusEnum transactionStatusEnum) {
		System.out.println(" fetching transaction status handler for -> "+transactionStatusEnum);
		
		switch(transactionStatusEnum) {
		case CREATED:
			return context.getBean(CreatedTransactionStatusHandler.class);
		
		case PENDING:
			return context.getBean(PendingTransactionStatusHandler.class);
			
		case INITATED:
			return context.getBean(InitiatedTransactionStatusHandler.class);

		case FAILED:
			return context.getBean(FailedTransactionStatusHandler.class);

		case APPROVED:
			return context.getBean(ApprovedTransactionStatusHandler.class);
			
		default:
			return null;
		}
	}
	
}
