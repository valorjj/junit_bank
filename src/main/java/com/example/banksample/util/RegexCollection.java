package com.example.banksample.util;


/**
 * 환경변수로 주입받는 값을 보관한다.
 * 리프레시 토큰 고려 X
 */
public abstract class RegexCollection {

	private RegexCollection() {
	}

	public static final String USER_FULL_NAME = "^[a-zA-Z가-힣]{1,10}\\s[a-zA-Z가-힣]{2,20}$";
	public static final String USER_NAME = "^[a-zA-Z0-9가-힣]{1,10}$";
	public static final String USER_EMAIL = "^[a-zA-Z0-9]{2,16}@[a-zA-Z0-9]{2,16}\\.[a-zA-Z]{2,3}$";
	public static final String TRANSFER_TEL = "^01([0|1|6|7|8|9])-?(\\d{3,4})-?(\\d{4})$";

}
