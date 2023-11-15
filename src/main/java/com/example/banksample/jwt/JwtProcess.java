package com.example.banksample.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.banksample.auth.LoginUser;
import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class JwtProcess {

	private JwtProcess() {

	}

	public static String createToken(LoginUser loginUser) {
		String jwtToken = JWT.create()
			// 토큰의 이름
			.withSubject("junit-bank-jwt")
			.withIssuer("local")
			.withExpiresAt(new Date(System.currentTimeMillis() + JwtTokenVO.TOKEN_EXP_TIME))
			.withClaim("id", loginUser.getUser().getId())
			.withClaim("role", String.valueOf(loginUser.getUser().getRole()))
			.sign(Algorithm.HMAC512(JwtTokenVO.TOKEN_SECRET));

		log.info("[*] jwtToken -> {}", jwtToken);
		return JwtTokenVO.TOKEN_PREFIX + jwtToken;
	}

	/**
	 * 토큰을 검증한다.
	 * 생성과 검증을 한 곳에서 하기 때문에 대칭키 알고리즘으로 간단하게 구현한다.
	 * 토큰 검증에 성공 시 LoginUser 객체를 반환하고, 해당 객체를
	 * 시큐리티 세션에 직접 주입시킨다.
	 */
	public static LoginUser verifyToken(String token) {
		DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtTokenVO.TOKEN_SECRET))
			.withIssuer("local")
			.build()
			.verify(token);

		Long id = decodedJWT.getClaim("id").asLong();
		String role = decodedJWT.getClaim("role").asString();
		User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
		return new LoginUser(user);
	}

}
