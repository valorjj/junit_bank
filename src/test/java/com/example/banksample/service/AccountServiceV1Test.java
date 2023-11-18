package com.example.banksample.service;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.transaction.TransactionEnum;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.TransactionRepository;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;

import static com.example.banksample.dto.account.AccountRequestDTO.*;
import static com.example.banksample.dto.account.AccountResponseDTO.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AccountServiceV1Test extends DummyObject {

	@InjectMocks
	private AccountServiceV1 accountServiceV1;
	@Mock
	private UserRepository userRepository;
	@Mock
	private AccountRepository accountRepository;
	@Mock
	private TransactionRepository transactionRepository;
	@Spy
	private ObjectMapper om;


	/**
	 * 신규 계좌 등록 테스트
	 */
	@Test
	void register_account_test() throws JsonProcessingException {
		// given
		Long userId = 1L;

		AccountSaveRequestDTO accountSaveRequestDTO = new AccountSaveRequestDTO();
		accountSaveRequestDTO.setAccountNumber(1111L);
		accountSaveRequestDTO.setAccountPassword(1111L);

		// stub - 1
		User newMockUser = newMockUser(userId, "jeongjin", "kim jeongjin");
		when(userRepository.findById(any())).thenReturn(Optional.of(newMockUser));

		// stub - 2
		when(accountRepository.findByNumber(any())).thenReturn(Optional.empty());

		// stub - 3
		Account newMockUserAccount = newMockAccount(1L, 1111L, 1000L, newMockUser);
		when(accountRepository.save(any())).thenReturn(newMockUserAccount);

		// when
		AccountSaveResponseDTO accountSaveResponseDTO = accountServiceV1.registerAccount(accountSaveRequestDTO, userId);
		String responseBody = om.writeValueAsString(accountSaveResponseDTO);
		log.info("responseBody -> {}", responseBody);

		// then


	}

	/**
	 * 계좌 조회 테스트
	 * 서비스레이어 테스트는 가짜 repo 를 주입받기 때문에 미리 데이터를 저장하는게 힘들 것 같은데
	 * 목록 조회는 대체 어떻게 해야하나?
	 */
	@Test
	void get_accounts_by_userId_test() throws Exception {
		// given
		Long userId = 1L;
		User newMockUser = newMockUser(userId, "jeongjin", "kim jeongjin");

		// registerAccount() 메서드에 의존하는 것 보다는
		// 직접 repository 에 값을 넣는게 좋아 보이는데,
		// 값이 안 들어간다! @Mock 때문이겠지?


		// stub-1
		// 유저가 존재하는 지 확인
		when(userRepository.findById(any())).thenReturn(Optional.of(newMockUser));

		AccountListResponseDTO accounts = accountServiceV1.getAccountsByUser(userId);
		String responseBody = om.writeValueAsString(accounts);
		log.info("[*] responseBody -> {}", responseBody);

		// 계좌가 존재하는 지 여부 확인
		// 계좌가 존재하는 경우, 계좌 총 개수 확인
		// 현재
		Assertions.assertThat(accounts.getAccounts()).isEmpty();
	}

	/**
	 * 계좌 삭제 테스트
	 */
	@Test
	void delete_account_by_number_test() throws Exception {
		// given
		Long number = 1001L;
		Long userId = 1L;

		// stub - 1
		User newMockuser = newMockUser(userId, "jeongjin", "kim jeongjin");
		Account newMockAccount = newMockAccount(1L, 1001L, 1000L, newMockuser);
		when(accountRepository.findByNumber(any())).thenReturn(Optional.of(newMockAccount));

		// when
		Assertions.assertThatThrownBy(() -> accountServiceV1.deleteAccount(number, userId))
				.isInstanceOf(CustomApiException.class);

		// then
		// fail 을 기대한다.
		// fail("예외가 발생하지 않음");

	}

	/**
	 * 입금 성공 테스트
	 * 1. Account -> balance 변경이 됐는지
	 * 2. Transaction -> balance 잘 기록됐는지
	 */
	@Test
	void deposit_success_test_01() throws Exception {
		// given
		// requestDTO
		AccountDepositRequestDTO accountDepositRequestDTO = new AccountDepositRequestDTO();
		accountDepositRequestDTO.setNumber(1001L);
		accountDepositRequestDTO.setAmount(100L);
		accountDepositRequestDTO.setTel("010-1234-5678");
		accountDepositRequestDTO.setType(String.valueOf(TransactionEnum.DEPOSIT));


		// stub - 1
		// 입금계좌 존재 여부 확인
		User newMockuser = newMockUser(1L, "jeongjin", "kim jeongjin");
		Account newMockAccount = newMockAccount(1L, 1001L, 1000L, newMockuser);
		when(accountRepository.findByNumber(any())).thenReturn(Optional.ofNullable(newMockAccount));

		// stub - 2
		// 입금

		/*
		 * stub 은 독립적인 상태를 유지해야 함을 기억하자. Account 객체를 연속해서 사용하는 경우,
		 * 값을 주입받는 시점의 불일치로 인해 원치 않은 값이 들어갈 수 있다.
		 * */
		Account newMockAccount2 = newMockAccount(1L, 1001L, 1000L, newMockuser);
		Transaction newMockDepositTransaction = newMockDepositTransaction(1L, newMockAccount2);
		when(transactionRepository.save(any())).thenReturn(newMockDepositTransaction);

		// when
		// 입금
		AccountDepositResponseDTO accountDepositResponseDTO = accountServiceV1.depositAccount(accountDepositRequestDTO);
		log.info("트랜잭션 입금 계좌1 잔액 -> {}", accountDepositResponseDTO.getTransaction().getDepositAccountBalance());
		log.info("입금 계좌1 잔액 -> {}", Objects.requireNonNull(newMockAccount).getBalance());
		log.info("입금 계좌2 잔액 -> {}", Objects.requireNonNull(newMockAccount2).getBalance());

		// then
		Assertions.assertThat(newMockAccount.getBalance()).isEqualTo(1100L);
		Assertions.assertThat(accountDepositResponseDTO.getTransaction().getDepositAccountBalance()).isEqualTo(1000L);


	}


	/*
	 * 서비스 레이어에서 정확히 어떤 걸 테스트 해야 하는걸까?
	 * 최대한 메모리를 적게 사용하고
	 * - 0원 입금 막히는 로직이 잘 작동하는지
	 * - 입금이후 금액이 기대하는 대로 증가했는지
	 * 여부를 확인해야 한다.
	 *
	 * 바로 위 테스트에 포함된 각종 DTO 를 생성하는 로직이 과연 서비스 레이어 테스트에서
	 * 꼭 필요한걸까?
	 *
	 * */


	@Test
	void account_withdraw_test() {
		// given
		// Account 생성 -> amount 만 사용하면 되니, 필요없는 과정이라 생략
		//		WithdrawAccountRequestDTO accountWithdrawRequestDTO = new WithdrawAccountRequestDTO();
		//		accountWithdrawRequestDTO.setNumber(1001L);
		//		accountWithdrawRequestDTO.setPassword(1234L);
		//		accountWithdrawRequestDTO.setType("WITHDRAW");
		//		accountWithdrawRequestDTO.setAmount(100L);

		long amount = 100L;
		long password = 1234L;
		long userId = 1L;

		User newMockuser = newMockUser(1L, "jeongjin", "kim jeongjin");
		Account newMockAccount = newMockAccount(1L, 1001L, 1000L, newMockuser);

		// 0원 체크
		// 이미 amount 는 0보다 큰 값으로 존재한다는 가정하에 하는거라 필요없음
		//		if (amount <= 0L) {
		//			throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
		//		}

		// 출금 소유자 확인
		newMockAccount.checkOwner(userId);
		newMockAccount.checkPassword(password);
		newMockAccount.withdraw(amount);

		Assertions.assertThat(newMockAccount.getBalance()).isEqualTo(900L);

	}

	/**
	 * 이체 테스트
	 *
	 *
	 */
	@Test
	void transfer_account_test() throws Exception {
		// given
		TransferAccountRequestDTO transferAccountRequestDTO = new TransferAccountRequestDTO();
		transferAccountRequestDTO.setWithdrawNumber(1001L);
		transferAccountRequestDTO.setDepositNumber(2001L);
		transferAccountRequestDTO.setWithdrawPassword(1234L);
		transferAccountRequestDTO.setAmount(100L);
		transferAccountRequestDTO.setType("TRANSFER");

		User newMockUser1 = newMockUser(1L, "jeongjin", "kim");
		User newMockUser2 = newMockUser(2L, "bird", "king");

		Account withdrawAccount = newMockAccount(1L, 1001L, 1000L, newMockUser1);
		Account depositAccount = newMockAccount(2L, 2001L, 1000L, newMockUser2);

		// 출금 계좌, 입금 계좌 동일한 지 여부 체크
		if (transferAccountRequestDTO.getWithdrawNumber().longValue() == transferAccountRequestDTO.getDepositNumber().longValue()) {
			throw new CustomApiException("입금 계좌와 출금 계좌가 동일할 수 없습니다.");
		}

		// 0원 체크
		if (transferAccountRequestDTO.getAmount() <= 0L) {
			throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
		}

		// 출금 소유자 확인
		withdrawAccount.checkOwner(1L);

		// 출금 계좌 비밀번호 확인
		withdrawAccount.checkPassword(transferAccountRequestDTO.getWithdrawPassword());

		// 이체하기
		withdrawAccount.withdraw(transferAccountRequestDTO.getAmount());
		depositAccount.deposit(transferAccountRequestDTO.getAmount());

		// then
		Assertions.assertThat(withdrawAccount.getBalance()).isEqualTo(900L);
		Assertions.assertThat(depositAccount.getBalance()).isEqualTo(1100L);
	}
}