package com.example.banksample.config.dummy;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DummyDevInit extends DummyObject implements CommandLineRunner {
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final EntityManager em;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		User user1 = newUser("jeongjin", "kim jeongjin");
		User user2 = newUser("bird", "king bird");
		Account account1 = newAccount(1001L, user1);
		Account account2 = newAccount(1002L, user1);
		Account account3 = newAccount(1003L, user2);
		Account account4 = newAccount(1004L, user2);
		Account account5 = newAccount(1005L, user2);
		user1.add(account1);
		user1.add(account2);
		user2.add(account3);
		user2.add(account4);
		user2.add(account5);
		userRepository.save(user1);
		userRepository.save(user2);
		accountRepository.save(account1);
		accountRepository.save(account2);
		accountRepository.save(account3);
		accountRepository.save(account4);
		accountRepository.save(account5);
	}
}
