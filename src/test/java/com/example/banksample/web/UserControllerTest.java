package com.example.banksample.web;

import com.example.banksample.config.dummy.DummyObject;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.example.banksample.dto.user.UserRequestDTO.JoinRequestDTO;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
@Transactional
class UserControllerTest extends DummyObject {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper om;
	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void init() {
		inputTestData();
	}

	@Test
	@DisplayName("회원가입_성공")
	void join_success_test() throws Exception {
		// given
		JoinRequestDTO joinRequestDTO = JoinRequestDTO.builder()
			.username("jeongjin")
			.password("1234")
			.email("admin@nate.com")
			.fullname("kim jeongjin")
			.build();

		String requestBody = om.writeValueAsString(joinRequestDTO);

		// when
		ResultActions resultActions
			= mockMvc.perform(MockMvcRequestBuilders
			.post("/api/signUp")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
		);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		// then
		resultActions.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("회원가입_실패")
	void join_fail_test() throws Exception {
		// given
		JoinRequestDTO joinRequestDTO = JoinRequestDTO.builder()
			.username("jeongjin")
			.fullname("kim jeongjin")
			.password("1234")
			.email("admin@nate.com")
			.build();

		String requestBody = om.writeValueAsString(joinRequestDTO);

		// when
		ResultActions resultActions
			= mockMvc.perform(MockMvcRequestBuilders
			.post("/api/signUp")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
		);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	void inputTestData() {
		// [해결] Unique index or primary key violation 에러 발생
		// userRepository.deleteAll();
		userRepository.save(newUser("jeongjin", "kim jeongjin"));
	}
}