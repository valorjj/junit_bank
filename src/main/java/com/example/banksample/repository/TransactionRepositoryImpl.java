package com.example.banksample.repository;

import com.example.banksample.domain.transaction.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface TransactionDAO {
	List<Transaction> findTransactionList(
			@Param("accountId") Long accountId,
			@Param("type") String type,
			@Param("page") Integer page
	);
}


/**
 * TransactionRepository + Impl 은 규칙
 */
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionDAO {

	private final EntityManager em;


	/*
	 * JPQL 동적쿼리 생성
	 * */
	@Override
	public List<Transaction> findTransactionList(Long accountId, String type, Integer page) {
		// type 별로 쿼리문 작성을 별도의 메서드로 분리
		String sql = getString(type);
		TypedQuery<Transaction> query = em.createQuery(sql, Transaction.class);

		switch (type) {
			case "WITHDRAW" -> query = query.setParameter("withdrawAccountId", accountId);
			case "DEPOSIT" -> query = query.setParameter("depositAccountId", accountId);
			default -> {
				query = query.setParameter("withdrawAccountId", accountId);
				query = query.setParameter("depositAccountId", accountId);
			}
		}

		// 페이지 설정
		query.setFirstResult(page * 5);
		query.setMaxResults(5);

		return query.getResultList();
	}

	private static String getString(String type) {
		String sql = "";
		sql += "SELECT t FROM Transaction t ";

		switch (type) {
			case "WITHDRAW" -> {
				sql += "JOIN FETCH t.withdrawAccount wa ";
				sql += "WHERE t.withdrawAccount.id = :withdrawAccountId";
			}
			case "DEPOSIT" -> {
				sql += "JOIN FETCH t.depositAccount da ";
				sql += "WHERE t.depositAccount.id = :depositAccountId";
			}
			default -> {
				sql += "LEFT JOIN FETCH t.withdrawAccount wa ";
				sql += "LEFT JOIN FETCH t.depositAccount da ";
				sql += "WHERE t.withdrawAccount.id = :withdrawAccountId ";
				sql += "OR ";
				sql += "t.depositAccount.id = :depositAccountId";
			}
		}

		return sql;
	}
}
