package com.example.banksample.service;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
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

import java.util.Optional;

import static com.example.banksample.dto.account.AccountRequestDTO.AccountSaveRequestDTO;
import static com.example.banksample.dto.account.AccountResponseDTO.AccountListResponseDTO;
import static com.example.banksample.dto.account.AccountResponseDTO.AccountSaveResponseDTO;
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
}