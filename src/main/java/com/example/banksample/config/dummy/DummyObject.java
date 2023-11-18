package com.example.banksample.config.dummy;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.transaction.TransactionEnum;
import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import com.example.banksample.repository.AccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DummyObject {

	/**
	 * 엔티티를 데이터베이스에 저장할 때 사용한다.
	 *
	 * @param username
	 * @param fullname
	 * @return
	 */
	protected static User newUser(String username, String fullname) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encPassword = passwordEncoder.encode("1234");
		return User.builder()
				.username(username)
				.email("mockuser@nate.com")
				.password(encPassword)
				.fullname(fullname)
				.role(UserEnum.CUSTOMER)
				.build();
	}

	/**
	 * 테스트 객체에서 stub 용도로 사용한다.
	 *
	 * @param id
	 * @param username
	 * @param fullname
	 * @return
	 */
	protected static User newMockUser(Long id, String username, String fullname) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encPassword = passwordEncoder.encode("1234");
		return User.builder()
				.id(id)
				.username(username)
				.email("mockuser@nate.com")
				.password(encPassword)
				.fullname(fullname)
				.role(UserEnum.CUSTOMER)
				.build();
	}

	/**
	 * Test 환경에서 사용
	 */
	protected static Account newAccount(
			Long number,
			User user
	) {
		return Account.builder()
				.number(number)
				.balance(1000L)
				.password(1234L)
				.user(user)
				.build();
	}


	/**
	 * DB 조회 시 사용
	 */
	protected static Account newMockAccount(
			Long id,
			Long number,
			Long balance,
			User user
	) {
		return Account.builder()
				.id(id)
				.balance(balance)
				.number(number)
				.password(1234L)
				.user(user)
				.build();
	}

	protected Transaction newDepositTransaction(
			Account account,
			AccountRepository accountRepository
	) {
		account.deposit(100L); // 1000원이 있었다면 900원이 됨

		/*
		 * 서비스 레이어에서 작동되는 것이 아니기 때문에
		 * 더티 체킹이 일어나지 않는다?
		 * */
		if (accountRepository != null) {
			accountRepository.save(account);
		}

		return Transaction.builder()
				.withdrawAccount(null)
				.depositAccount(account)
				.withdrawAccountBalance(null)
				.depositAccountBalance(account.getBalance())
				.amount(100L)
				.type(TransactionEnum.DEPOSIT)
				.sender("ATM")
				.receiver(String.valueOf(account.getNumber()))
				.tel("010-1234-5678")
				.build();
	}

	protected static Transaction newMockDepositTransaction(
			Long id,
			Account account
	) {
		/*
		 * 트랜잭션 히스토리를 생성하기 위해서는 입금, 혹은 출금 등의 과정이 발생해야 한다.
		 * 해당 코드를 매번 테스트 케이스마다 작성하지 않고 트랜잭션 히스토리를 1건 생성하기 위해
		 * 10원을 입금한다.
		 * */
		account.deposit(10L);

		return Transaction.builder()
				.id(id)
				.depositAccount(account)
				.withdrawAccount(null)
				.depositAccountBalance(account.getBalance())
				.withdrawAccountBalance(null)
				.amount(1000L)
				.sender("ATM")
				.receiver(String.valueOf(account.getNumber()))
				.tel("010-1234-5678")
				.build();
	}

	protected Transaction newWithdrawTransaction(Account account
			, AccountRepository accountRepository) {
		account.withdraw(100L);

		// Repository Test에서는 더티체킹 됨
		// Controller Test에서는 더티체킹 안됨
		if (accountRepository != null) {
			accountRepository.save(account);
		}

		return Transaction.builder()
				.withdrawAccount(account)
				.depositAccount(null)
				.withdrawAccountBalance(account.getBalance())
				.depositAccountBalance(null)
				.amount(100L)
				.type(TransactionEnum.WITHDRAW)
				.sender(String.valueOf(account.getNumber()))
				.receiver("ATM")
				.build();
	}

	protected Transaction newTransferTransaction(
			Account withdrawAccount,
			Account depositAccount,
			AccountRepository accountRepository
	) {

		withdrawAccount.withdraw(100L);
		depositAccount.deposit(100L);

		/*
		 * 더티 체킹이 일어나지 않는다. (왜?)
		 * 강제로 저장한다.
		 * */
		if (accountRepository != null) {
			accountRepository.save(withdrawAccount);
			accountRepository.save(depositAccount);
		}

		return Transaction.builder()
				.withdrawAccount(withdrawAccount)
				.depositAccount(depositAccount)
				.withdrawAccountBalance(withdrawAccount.getBalance())
				.depositAccountBalance(depositAccount.getBalance())
				.amount(100L)
				.type(TransactionEnum.TRANSFER)
				.sender(String.valueOf(withdrawAccount.getNumber()))
				.receiver(String.valueOf(depositAccount.getNumber()))
				.build();
	}


}
