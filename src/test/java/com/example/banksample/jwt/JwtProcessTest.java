package com.example.banksample.jwt;

import com.example.banksample.auth.LoginUser;
import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * 환경변수를 읽어오는데, 해당 환경변수는 IOC 생성 이후에 값이 주입된다.
 * 따라서 내키지 않지만 @SpringBootTest 를 통해 빈을 주입받은 채로 테스트를 진행한다.
 */
@Slf4j
@SpringBootTest
class JwtProcessTest {

	@Test
	void create_test() throws Exception {
		// given
		User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
		LoginUser loginUser = new LoginUser(user);

		// when
		String jwtToken = JwtProcess.createToken(loginUser);
		log.info("jwtToken -> {}", jwtToken);

		// then
		Assertions.assertThat(jwtToken).startsWith(JwtTokenVO.TOKEN_PREFIX);
	}


	@Test
	void verify_test() throws Exception {
		// given
		String givenToken = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdW5pdC1iYW5rLWp3dCIsImlzcyI6ImxvY2FsIiwiZXhwIjoxNzAwMTYxODkxLCJpZCI6MSwicm9sZSI6IkNVU1RPTUVSIn0.EsTQQagIWXVr7eNTWFtEi_M61NIl3RunSaJ2I0TRAbkz2yWGJffMw5ozb7DCaq4MngVKkgdqWQQqqJXcrhRr3g";

		// when
		LoginUser loginUser = JwtProcess.verifyToken(givenToken);
		log.info("id -> {}", loginUser.getUser().getId());
		log.info("role -> {}", loginUser.getUser().getRole());

		// then
		Assertions.assertThat(loginUser.getUser().getId()).isEqualTo(1L);
		Assertions.assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
	}

}