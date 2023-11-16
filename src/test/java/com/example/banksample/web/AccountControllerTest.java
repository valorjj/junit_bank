package com.example.banksample.web;


import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.domain.user.User;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.example.banksample.dto.account.AccountRequestDTO.AccountSaveRequestDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
@Transactional
class AccountControllerTest extends DummyObject {
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ObjectMapper om;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EntityManager em;


	@BeforeEach
	void init() {
		User jeongjin = userRepository.save(newUser("test", "kim jeongjin"));
		User bori = userRepository.save(newUser("bori", "kim jeongjin"));
		accountRepository.save(newAccount(1001L, jeongjin));
		accountRepository.save(newAccount(1002L, bori));
		em.clear();
	}


	/**
	 * 현재까지 로그인 흐름은 아래와 같다.
	 * jwt 토큰을 전달 -> 인증 필터 -> 시큐리티 컨텍스트에 보관할 세션 생성
	 * 헤더에 jwt 토큰이 없는 경우에도 필터를 계속 타고 가서 컨트롤러에 접근을 시도한다. 하지만! authorizeHttpRequests 에서 최종적으로 거부당한다.
	 * 따라서, 테스트 환경에서 로그인을 하기 위해서는 jwt 토큰 생성 단계는 건너뛰고 시큐리티 세션만 생성하면 된다.
	 * {@code @WithUserDetails(value = "jeongjin")} 를 사용하면, DB 에서 'jeongjin' 이라는 유저를 찾고 세션에 담긴다.
	 * <p>
	 * 다만, 해당 유저를 @BeforeEach 로 넣어주는 로직을 넣어도 실패한다. @WithUserDetails 는 기본적으로 TEST_METHOD 옵션으로 지정되어 있어
	 * 다른 메서드 실행보다 더 우선적으로 실행되기 때문이다. 따라서 DB 에 테스트 객체를 넣기 전에 DB 를 조회해서 오류가 난다.
	 * 따라서 오류를 해결하기 위해 아래와 같은 옵션을 부여한다.
	 * {@code @WithUserDetails(value = "jeongjin", setupBefore = TestExecutionEvent.TEST_EXECUTION)}
	 */
	@WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@Test
	void register_account_test() throws Exception {

		// given
		AccountSaveRequestDTO accountSaveRequestDTO = new AccountSaveRequestDTO();
		accountSaveRequestDTO.setAccountNumber(9999L);
		accountSaveRequestDTO.setAccountPassword(123456L);
		String requestBody = om.writeValueAsString(accountSaveRequestDTO);
		log.info("[*] requestBody -> {}", requestBody);

		// when
		ResultActions resultActions = mockMvc.perform(post("/api/test/account").content(requestBody).contentType(MediaType.APPLICATION_JSON));
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		// then
		assertThat(status().isCreated());


	}

	@Test
	@WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void delete_account_test() throws Exception {
		// given
		Long number = 1001L;

		// when
		ResultActions resultActions = mockMvc.perform(delete("/api/test/account/" + number));
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

	}
}