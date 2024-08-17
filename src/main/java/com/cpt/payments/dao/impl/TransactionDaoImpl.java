package com.cpt.payments.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.utils.LogMessage;

@Repository
public class TransactionDaoImpl implements TransactionDao {

	private static final Logger LOGGER = LogManager.getLogger(TransactionDaoImpl.class);
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public Transaction createTransaction(Transaction transaction) {

		String sql = "INSERT INTO payments.Transaction (userId, paymentMethodId, providerId, paymentTypeId, amount, currency, txnStatusId, txnReference, txnDetailsId, " +
				"providerCode, providerMessage, debitorAccount, creditorAccount, providerReference, merchantTransactionReference, retryCount) " +
				"VALUES (:userId, :paymentMethodId, :providerId, :paymentTypeId, :amount, :currency, :txnStatusId, :txnReference, :txnDetailsId, " +
				":providerCode, :providerMessage, :debitorAccount, :creditorAccount, :providerReference, :merchantTransactionReference, :retryCount)";

		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("userId", transaction.getUserId())
				.addValue("paymentMethodId", transaction.getPaymentMethodId())
				.addValue("providerId", transaction.getProviderId())
				.addValue("paymentTypeId", transaction.getPaymentTypeId())
				.addValue("amount", transaction.getAmount())
				.addValue("currency", transaction.getCurrency())
				.addValue("txnStatusId", transaction.getTxnStatusId())
				.addValue("txnReference", transaction.getTxnReference())
				.addValue("txnDetailsId", transaction.getTxnDetailsId())
				.addValue("providerCode", transaction.getProviderCode())
				.addValue("providerMessage", transaction.getProviderMessage())
				.addValue("debitorAccount", transaction.getDebitorAccount())
				.addValue("creditorAccount", transaction.getCreditorAccount())
				.addValue("providerReference", transaction.getProviderReference())
				.addValue("merchantTransactionReference", transaction.getMerchantTransactionReference())
				.addValue("retryCount", transaction.getRetryCount());

		try {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			int rowUpdated = jdbcTemplate.update(sql, params, keyHolder);
			
			transaction.setId(keyHolder.getKey().intValue());
			System.out.println("Successfully Inserted data|rowUpdated:" + rowUpdated);
		} catch (Exception e) {
			System.out.println("Exception createTransaction:" + e);
			return null;
		}

		return transaction;
	}
	
	@Override
	public boolean updateTransaction(Transaction transaction) {
		try {
			jdbcTemplate.update(updateTransaction(), new BeanPropertySqlParameterSource(transaction));
			return true;
		} catch (Exception e) {
			System.out.println("exception while updating TRANSACTION in DB :: " + transaction);
		}
		return false;
	}

	private String updateTransaction() {
		StringBuilder queryBuilder = new StringBuilder("Update Transaction ");
		queryBuilder.append("SET txnStatusId=:txnStatusId, providerCode=:providerCode, providerMessage=:providerMessage ");
		queryBuilder.append("WHERE id=:id ");
		System.out.println(" " + "update Transaction query -> " + queryBuilder);
		return queryBuilder.toString();
	}

	@Override
	public Transaction getTransactionById(long transactionId) {
		System.out.println(" :: fetching Transaction Details  for :: " + transactionId);

		Transaction transaction = null;
		try {
			transaction = jdbcTemplate.queryForObject(getTransactionById(),
					new BeanPropertySqlParameterSource(Transaction.builder().id((int) transactionId).build()),
					new BeanPropertyRowMapper<>(Transaction.class));
			System.out.println(" :: transaction Details from DB  = " + transaction);
		} catch (Exception e) {
			System.out.println("unable to get transaction Details " + e);
		}
		return transaction;
	}

	private String getTransactionById() {
		StringBuilder queryBuilder = new StringBuilder("Select * from Transaction where id=:id ");
		System.out.println(" " + "getTransactionById query -> " + queryBuilder);
		return queryBuilder.toString();
	}
	
	@Override
	public void updateProviderReference(Transaction transaction) {
		try {
			jdbcTemplate.update(updateProviderReference(), new BeanPropertySqlParameterSource(transaction));
		} catch (Exception e) {
			LogMessage.log(LOGGER, "exception while updating TRANSACTION in DB :: " + transaction);
			LogMessage.logException(LOGGER, e);
		}

	}

	private String updateProviderReference() {
		StringBuilder queryBuilder = new StringBuilder("Update Transaction ");
		queryBuilder.append("SET providerReference=:providerReference ");
		queryBuilder.append("WHERE id=:id ");
		LogMessage.log(LOGGER, " " + "updateProviderReference query -> " + queryBuilder);
		return queryBuilder.toString();
	}

	@Override
	public Transaction getTransactionByProviderReference(String paymentId) {
		LogMessage.log(LOGGER, " :: fetching Transaction Details  for provider reference :: " + paymentId);

		Transaction transaction = null;
		try {
			transaction = jdbcTemplate.queryForObject(getTransactionByProviderReference(),
					new BeanPropertySqlParameterSource(Transaction.builder().providerReference(paymentId).build()),
					new BeanPropertyRowMapper<>(Transaction.class));
			LogMessage.log(LOGGER, " :: transaction Details from DB  = " + transaction);
		} catch (Exception e) {
			LogMessage.log(LOGGER, "unable to get transaction Details " + e);
			LogMessage.logException(LOGGER, e);
		}
		return transaction;
	}

	private String getTransactionByProviderReference() {
		StringBuilder queryBuilder = new StringBuilder(
				"Select * from Transaction where providerReference=:providerReference ");
		LogMessage.log(LOGGER, "getTransactionByProviderReference query -> " + queryBuilder);
		return queryBuilder.toString();
	}
	
	@Override
	public List<Transaction> fetchAllTransactionsForReconcilation() {
		LogMessage.log(LOGGER, " :: fetching Transaction Details  for retry :: ");

		List<Transaction> transaction = new ArrayList<>();
		try {
			transaction = jdbcTemplate.query(fetchTransactionsForReconcilation(),
					new BeanPropertySqlParameterSource(Transaction.builder().build()),
					new BeanPropertyRowMapper<>(Transaction.class));
			LogMessage.log(LOGGER, " :: transaction Details from DB  = " + transaction);
		} catch (Exception e) {
			LogMessage.log(LOGGER, "unable to get transaction Details " + e);
			LogMessage.logException(LOGGER, e);
		}
		return transaction;
	}

	private String fetchTransactionsForReconcilation() {
		StringBuilder queryBuilder = new StringBuilder("select * from Transaction ");
		queryBuilder.append("where retryCount < 3 and txnStatusId = 2");
		LogMessage.log(LOGGER, " " + "fetchTransactionsForReconcilation query -> " + queryBuilder);
		return queryBuilder.toString();
	}

	@Override
	public void updateRetryCount(Transaction transaction) {
		try {
			transaction.setRetryCount(transaction.getRetryCount()+1);
			jdbcTemplate.update(updateRetryCountQuery(), new BeanPropertySqlParameterSource(transaction));
		} catch (Exception e) {
			LogMessage.log(LOGGER, "exception while updating TRANSACTION in DB :: " + transaction);
			LogMessage.logException(LOGGER, e);
		}

	}

	private String updateRetryCountQuery() {
		StringBuilder queryBuilder = new StringBuilder("Update Transaction ");
		queryBuilder.append("SET retryCount=:retryCount ");
		queryBuilder.append("WHERE id=:id ");
		LogMessage.log(LOGGER, " " + "updateProviderCodeAndMessage query -> " + queryBuilder);
		return queryBuilder.toString();
	}

}
