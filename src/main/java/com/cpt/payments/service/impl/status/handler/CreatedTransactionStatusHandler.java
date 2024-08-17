package com.cpt.payments.service.impl.status.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dao.TransactionLogDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.dto.TransactionLog;
import com.cpt.payments.service.TransactionStatusHandler;

@Service
public class CreatedTransactionStatusHandler extends TransactionStatusHandler {

	@Autowired
	TransactionDao transactionDao;
	
	@Autowired
	TransactionLogDao transactionLogDao;
	
	@Override
	public boolean updateStatus(Transaction transaction) {
		System.out.println("CreatedTransactionStatusHandler.updateStatus() invoked with transaction:" + transaction);

		transaction.setTxnDetailsId(TransactionStatusEnum.CREATED.getId());
		Transaction txnResponse = transactionDao.createTransaction(transaction);
		
		if(txnResponse == null) {
			System.out.println("Failed to save transaction");
			return false;
		}
		
		updateTxnLog(
				txnResponse.getId(), 
				"-", 
				TransactionStatusEnum.CREATED.getName());
		
		System.out.println("Transaction saved successfully");
		return true;
	}

}
