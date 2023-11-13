package com.example.banksample.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenVO {

	public static Integer TOKEN_EXP_TIME;
	public static String TOKEN_SECRET;
	public static String TOKEN_PREFIX;
	public static String TOKEN_HEADER;

	@Value("${jwt.exp_time}")
	public void setTokenExpTime(Integer TOKEN_EXP_TIME) {
		this.TOKEN_EXP_TIME = TOKEN_EXP_TIME;
	}

	@Value("${jwt.secret}")
	public void setTokenSecret(String TOKEN_SECRET) {
		this.TOKEN_SECRET = TOKEN_SECRET;
	}

	@Value("${jwt.token_prefix}")
	public void setTokenPrefix(String TOKEN_PREFIX) {
		this.TOKEN_PREFIX = TOKEN_PREFIX;
	}

	@Value("${jwt.header}")
	public void setTokenHeader(String TOKEN_HEADER) {
		this.TOKEN_HEADER = TOKEN_HEADER;
	}


}
