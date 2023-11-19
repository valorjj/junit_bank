package com.example.banksample.repository;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.user.User;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;


/**
 * 데이터베이스 관련 Bean 들을 불러오는 어노테이션을 사용한다.
 */
@DataJpaTest
@Slf4j
@ActiveProfiles("test")
class TransactionRepositoryImplTest extends DummyObject {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private EntityManager em;

	@BeforeEach
	void init() {
		insertData();
		autoIncrementReset();

		/*
		 * 더미 데이터 삽입 후, 원치 않은 데이터가 1차 캐시에 저장되어 있는 상태를
		 * 원치 않기 때문에 PC 초기화 필요!
		 * */
		em.clear();
	}

	@Test
	void find_all_transaction_list() {
		// given
		Long accountId = 1L;
		// when
		List<Transaction> transactions = transactionRepository.findTransactionList(accountId, "ALL", 0);
		for (Transaction transaction : transactions) {
			log.info("id:[{}] type -> {}", transaction.getId(), transaction.getType());
			log.info("id:[{}] amount -> {}", transaction.getId(), transaction.getAmount());
			log.info("id:[{}] sender -> {}", transaction.getId(), transaction.getSender());
			log.info("id:[{}] receiver -> {}", transaction.getId(), transaction.getReceiver());
			log.info("id:[{}] {} balance -> {}", transaction.getId(), transaction.getReceiver(), transaction.getDepositAccountBalance());
			log.info("id:[{}] {} balance -> {}", transaction.getId(), transaction.getSender(), transaction.getWithdrawAccountBalance());
			log.info("--- NEW LINE ---");
		}

	}


	@Test
	void data_test() {
		List<Transaction> transactions = transactionRepository.findAll();
		for (Transaction transaction : transactions) {
			log.info("id:[{}] type -> {}", transaction.getId(), transaction.getType());
			log.info("id:[{}] amount -> {}", transaction.getId(), transaction.getAmount());
			log.info("id:[{}] sender -> {}", transaction.getId(), transaction.getSender());
			log.info("id:[{}] receiver -> {}", transaction.getId(), transaction.getReceiver());
			log.info("id:[{}] {} balance -> {}", transaction.getId(), transaction.getReceiver(), transaction.getDepositAccountBalance());
			log.info("id:[{}] {} balance -> {}", transaction.getId(), transaction.getSender(), transaction.getWithdrawAccountBalance());
			log.info("--- NEW LINE ---");
		}
	}

	private void insertData() {
		User user1 = userRepository.save(newUser("jeongjin", "kim"));
		User user2 = userRepository.save(newUser("bird", "king"));
		User user3 = userRepository.save(newUser("cat", "king"));
		User user4 = userRepository.save(newUser("dog", "king"));

		Account account1 = accountRepository.save(newAccount(1001L, user1));
		Account account2 = accountRepository.save(newAccount(2001L, user2));
		Account account3 = accountRepository.save(newAccount(3001L, user3));
		Account account4 = accountRepository.save(newAccount(4001L, user1));

		Transaction transferTransaction1 = transactionRepository.save(
				newTransferTransaction(account1, account2, accountRepository));
		Transaction transferTransaction2 = transactionRepository.save(
				newTransferTransaction(account1, account3, accountRepository));
		Transaction transferTransaction3 = transactionRepository.save(
				newTransferTransaction(account2, account1, accountRepository));
		Transaction transferTransaction4 = transactionRepository.save(
				newTransferTransaction(account3, account1, accountRepository));
		Transaction transferTransaction5 = transactionRepository.save(
				newTransferTransaction(account2, account4, accountRepository));

		Transaction withdrawTransaction1 = transactionRepository.save(
				newWithdrawTransaction(account1, accountRepository));

		Transaction withdrawTransaction2 = transactionRepository.save(
				newWithdrawTransaction(account2, accountRepository));

		Transaction depositTransaction1 = transactionRepository.save(
				newDepositTransaction(account1, accountRepository));

		Transaction depositTransaction2 = transactionRepository.save(
				newDepositTransaction(account2, accountRepository));

	}


	/**
	 * PK 값을 1로 초기화 시킨다.
	 * {@code @DataJpaTest} 이 포함하고 있는 {@code @Transactional} 어노테이션 때문에
	 * TRUNCATE 해도 테이블이 초기화되지 않는다.
	 */
	private void autoIncrementReset() {
		em.createNativeQuery("ALTER TABLE tbl_user ALTER COLUMN id RESTART WITH 1").executeUpdate();
		em.createNativeQuery("ALTER TABLE tbl_account ALTER COLUMN id RESTART WITH 1").executeUpdate();
		em.createNativeQuery("ALTER TABLE tbl_transaction ALTER COLUMN id RESTART WITH 1").executeUpdate();
	}

}