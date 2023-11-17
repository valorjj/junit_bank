package com.example.banksample.jwt;

import com.example.banksample.auth.LoginUser;
import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")
class JwtAuthorizationFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("인증 성공 but 404 (토큰 존재)")
	void authorization_success_test() throws Exception {
		// given
		User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
		LoginUser loginUser = new LoginUser(user);
		String jwtToken = JwtProcess.createToken(loginUser);

		// when
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/test/hello").header(JwtTokenVO.TOKEN_HEADER, jwtToken));

		// then
		resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("인증 실패 (토큰이 없는 경우)")
	void authorization_fail_test() throws Exception {
		// given

		// when
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/test/hello"));

		// then
		resultActions.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	@DisplayName("인증 및 관리자 권한 인가 성공")
	void authorization_admin_success_test() throws Exception {
		// given
		User user = User.builder().id(1L).role(UserEnum.ADMIN).build();
		LoginUser loginUser = new LoginUser(user);
		String jwtToken = JwtProcess.createToken(loginUser);

		// when
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/hello").header(JwtTokenVO.TOKEN_HEADER, jwtToken));

		// then
		resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("인증 및 관리자 권한 인가 실패")
	void authorization_admin_fail_test() throws Exception {
		// given
		User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
		LoginUser loginUser = new LoginUser(user);
		String jwtToken = JwtProcess.createToken(loginUser);

		// when
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/hello").header(JwtTokenVO.TOKEN_HEADER, jwtToken));

		// then
		resultActions.andExpect(MockMvcResultMatchers.status().isForbidden());
	}
}