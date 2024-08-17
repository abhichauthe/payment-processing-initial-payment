package com.cpt.payments.service.impl.status.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.service.TransactionStatusHandler;
import com.cpt.payments.utils.LogMessage;

@Component
public class FailedTransactionStatusHandler extends TransactionStatusHandler {

	private static final Logger LOGGER = LogManager.getLogger(FailedTransactionStatusHandler.class);

	@Autowired
	private TransactionDao transactionDao;

	@Override
	public boolean updateStatus(Transaction transaction) {
		LogMessage.log(LOGGER, " transaction failed -> " + transaction);

		String fromStatus = TransactionStatusEnum.getTransactionStatusEnum(
				transactionDao.getTransactionById(transaction.getId()).getTxnStatusId()).getName();
		
		transaction.setTxnStatusId(TransactionStatusEnum.FAILED.getId());

		boolean transactionStatus = transactionDao.updateTransaction(transaction);
		if (!transactionStatus) {
			LogMessage.log(LOGGER, " updating transaction failed -> " + transaction);
			return false;
		}
		
		updateTxnLog(
				transaction.getId(), 
				fromStatus, 
				TransactionStatusEnum.FAILED.getName());
		
		return true;
	}

}
