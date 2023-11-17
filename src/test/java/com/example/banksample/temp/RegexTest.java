package com.example.banksample.temp;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

@Slf4j
class RegexTest {

	String value = "I am 신뢰에요 100%";

	@Test
	@DisplayName("한글만 통과")
	void only_korean_test() throws Exception {
		boolean result = Pattern.matches("^[가-힣]+$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	@DisplayName("한글 있으면 실패")
	void never_korean_test() throws Exception {
		boolean result = Pattern.matches("^[^가-힣]+$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	@DisplayName("영어만 통과")
	void only_english_test() throws Exception {
		boolean result = Pattern.matches("^[a-zA-Z]+$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	@DisplayName("영어 있으면 실패")
	void never_english_test() throws Exception {
		boolean result = Pattern.matches("^[^a-zA-Z]+$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	@DisplayName("영어와 숫자만 통과")
	void only_english_and_numbers_test() throws Exception {
		boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	@DisplayName("영어만 통과, 단 길이는 최소2 최소4")
	void only_english_length_test() throws Exception {
		boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);
		Assertions.assertThat(result).isTrue();
	}

	@Test
	void username_test() {
		String username = "jeongjin";
		boolean result = Pattern.matches("^\\w{2,20}$", username);
		log.info("[*] -> {}", result);
	}

	@Test
	void fullname_test() {
		String fullname = "최강jeongjin";
		boolean result = Pattern.matches("^[a-zA-Z가-힣]{2,20}$", fullname);
		log.info("[*] -> {}", result);
	}

	@Test
	void email_test_1() {
		String email = "admin@nate.com";
		boolean result = Pattern.matches("^\\w{2,6}@\\w{2,10}\\.[a-zA-z]{2,3}$", email);
		log.info("[*] -> {}", result);
	}

	@Test
	void email_test_2() {
		// 숫자, 영어로 시작하고
		// -_. 을 포함한 숫자, 영어만 존재하며
		// @ 가 존재한다.
		// 숫자, 영어로 다시 시작하고
		// -_. 포함한 영어, 숫자만 존재하고
		// . 이 들어간다.
		// 2 혹은 3 글자인 영어로 끝난다.

		String email = "fjwief-fw@nate.com";
		boolean result = Pattern.matches(
				"^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$",
				email
		);
		log.info("[*] -> {}", result);
	}

	@Test
	void account_type_test_1() {
		String type = "TRANSFER";
		boolean result = Pattern.matches("^(DEPOSIT)$", type);
		log.info("[*] -> {}", result);
	}

	@Test
	void account_type_test_2() {
		String type = "TRANSFER";
		boolean result = Pattern.matches("^(DEPOSIT|TRANSFER)$", type);
		log.info("[*] -> {}", result);
	}

	@Test
	void account_tel_test_1() {
		String tel = "010-1234-5678";
		boolean result = Pattern.matches("^\\d{11}", tel);
		log.info("[*] -> {}", result);
	}

	@Test
	void account_tel_test_2() {
		String tel = "010-1234-5678";
		boolean result = Pattern.matches("^01([0|1|6|7|8|9])-?(\\d{3,4})-?(\\d{4})$", tel);
		log.info("[*] -> {}", result);
	}


	/*
	 * 0원 X
	 * 숫자만 O
	 * */
	@Test
	void account_amount_test_1() {
		String amount = "100";
		boolean result = Pattern.matches("^[^0|\\S]$", amount);
		log.info("[*] -> {}", result);
	}
}
