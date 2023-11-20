package com.example.banksample.web;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.user.User;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.TransactionRepository;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
// @Transactional
@Sql("classpath:db/teardown.sql")
@Rollback
@ActiveProfiles("test")
class TransactionControllerTest extends DummyObject {

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private ObjectMapper om;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private EntityManager em;

	@BeforeEach
	void init() {
		insertData();
		em.clear();
	}

	@Test
	@WithUserDetails(value = "jeongjin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void find_transaction_list_test() throws Exception {
		long accountNumber = 1001L;
		String type = "ALL";
		String page = "0";

		ResultActions resultActions = mockMvc.perform(get("/api/test/account/" + accountNumber + "/transaction")
				.param("type", type)
				.param("page", page));
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(700L));
	}



	private void insertData() {
		User user1 = userRepository.save(newUser("jeongjin", "kim"));
		User user2 = userRepository.save(newUser("bird", "king"));
		User user3 = userRepository.save(newUser("cat", "king"));
		User user4 = userRepository.save(newUser("dog", "king"));

		Account account1 = accountRepository.save(newAccount(1001L, user1));
		Account account2 = accountRepository.save(newAccount(2001L, user2));
		Account account3 = accountRepository.save(newAccount(3001L, user3));
		Account account4 = accountRepository.save(newAccount(4001L, user4));

		Transaction transferTransaction1 = transactionRepository.save(
				newTransferTransaction(account1, account2, accountRepository));
		Transaction transferTransaction2 = transactionRepository.save(
				newTransferTransaction(account1, account3, accountRepository));
		Transaction transferTransaction3 = transactionRepository.save(
				newTransferTransaction(account2, account3, accountRepository));
		Transaction transferTransaction4 = transactionRepository.save(
				newTransferTransaction(account3, account4, accountRepository));
		Transaction transferTransaction5 = transactionRepository.save(
				newTransferTransaction(account1, account4, accountRepository));
	}
}