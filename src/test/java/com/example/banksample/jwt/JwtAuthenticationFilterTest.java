package com.example.banksample.jwt;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.dto.user.UserRequestDTO;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


/*
 * WebEnvironment.MOCK 설정하지 않으면, Mockito 환경에 ObjectMapper 객체가 생성되지 않아서
 * null 값이 들어간다.
 * AutoConfigureMockMvc 은 Mockito 환경에 MockMvc 객체를 주입한다.
 * */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
class JwtAuthenticationFilterTest extends DummyObject {

	@Autowired
	private ObjectMapper om;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void init() throws Exception {
		userRepository.deleteAll();
		userRepository.save(newUser("jeongjin", "kim jeongjin"));
	}

	@Test
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
		log.info("responseBody -> {}", responseBody);

	}

//	@Test
//	void fail_test() throws Exception {
//
//	}
}