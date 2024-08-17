package com.cpt.payments.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dao.TransactionLogDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.dto.TransactionLog;

public abstract class TransactionStatusHandler {
	
	@Autowired
	private TransactionLogDao transactionLogDao;

	public abstract boolean updateStatus(Transaction transaction);

	protected void updateTxnLog(int transactionId, String fromStatus, String toStatus) {
		System.out.println("updateTxnLog||transactionId:" + transactionId + "|fromStatus:" + fromStatus + "|toStatus:" + toStatus);
		
		TransactionLog transactionLog = TransactionLog.builder()
				.transactionId(transactionId)
				.txnFromStatus(fromStatus)
				.txnToStatus(toStatus)
				.build();
		
		transactionLogDao.createTransactionLog(transactionLog);
	}

}
