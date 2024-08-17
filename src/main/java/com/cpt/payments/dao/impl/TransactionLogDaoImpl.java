package com.cpt.payments.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cpt.payments.dao.TransactionLogDao;
import com.cpt.payments.dto.TransactionLog;

@Repository
public class TransactionLogDaoImpl implements TransactionLogDao {

	@Autowired
	private NamedParameterJdbcTemplate namedJdbcTemplate;

	@Override
	public void createTransactionLog(TransactionLog transactionLog) {

		try {
			namedJdbcTemplate.update(createTransactionLog(), new BeanPropertySqlParameterSource(transactionLog));
			System.out.println("TransactionLog created successfully");
		} catch (Exception e) {
			System.out.println("exception while saving transaction log in DB :: " + transactionLog);
		}

	}

	private String createTransactionLog() {
		StringBuilder queryBuilder = new StringBuilder("INSERT INTO Transaction_Log ");
		queryBuilder.append("(transactionId, txnFromStatus, txnToStatus) ");
		queryBuilder.append("VALUES(:transactionId, :txnFromStatus, :txnToStatus)");
		System.out.println(" Insert Transaction log query -> " + queryBuilder);
		return queryBuilder.toString();
	}

}
