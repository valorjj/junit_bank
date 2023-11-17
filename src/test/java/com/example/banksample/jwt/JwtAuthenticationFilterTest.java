package com.example.banksample.jwt;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.dto.user.UserRequestDTO;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * WebEnvironment.MOCK 설정하지 않으면, Mockito 환경에 ObjectMapper 객체가 생성되지 않아서
 * null 값이 들어간다.
 * AutoConfigureMockMvc 은 Mockito 환경에 MockMvc 객체를 주입한다.
 * */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")
class JwtAuthenticationFilterTest extends DummyObject {

	@Autowired
	private ObjectMapper om;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void init() throws Exception {
		userRepository.save(newUser("jeongjin", "kim jeongjin"));
	}

	@Test
	@DisplayName("로그인 성공")
	void successful_test() throws Exception {
		// given
		UserRequestDTO.LoginRequestDTO loginRequestDTO = new UserRequestDTO.LoginRequestDTO();
		loginRequestDTO.setUsername("jeongjin");
		loginRequestDTO.setPassword("1234");

		String requestBody = om.writeValueAsString(loginRequestDTO);
		log.info("requestBody -> {}", requestBody);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/login")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
			);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtTokenVO.TOKEN_HEADER);
		log.info("responseBody -> {}", responseBody);
		log.info("jwtToken -> {}", jwtToken);


		// then

		// 1. 정상 응답을 하는지 여부 확인
		resultActions.andExpect(MockMvcResultMatchers.status().isOk());
		Assertions.assertThat(jwtToken)
			// 2. 생성된 토큰 값이 null 이 아닌지 확인
			.isNotNull()
			// 3. 생성된 토큰 값이 'Bearer ' 인지 확인
			.startsWith(JwtTokenVO.TOKEN_PREFIX);
		// 4. 응답의 data.username 이 테스트 데이터로 입력한 값과 동일한 값인지 확인
		resultActions.andExpect(jsonPath("$.data.username").value("jeongjin"));
	}

	@Test
	@DisplayName("로그인 실패")
	void fail_test() throws Exception {
		// given
		UserRequestDTO.LoginRequestDTO loginRequestDTO = new UserRequestDTO.LoginRequestDTO();
		loginRequestDTO.setUsername("jeongjin");
		loginRequestDTO.setPassword("12345");

		String requestBody = om.writeValueAsString(loginRequestDTO);
		log.info("requestBody -> {}", requestBody);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/login")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
			);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtTokenVO.TOKEN_HEADER);
		log.info("responseBody -> {}", responseBody);
		log.info("jwtToken -> {}", jwtToken);

		// then
		// 로그인 실패 시, 401 에러 코드를 반환하는 지 여부를 확인한다.
		resultActions.andExpect(status().isUnauthorized());
	}
}