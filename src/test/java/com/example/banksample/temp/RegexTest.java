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
    void email_test() {
        String email = "admin@nate.com";
        boolean result = Pattern.matches("^\\w{2,6}@\\w{2,10}\\.[a-zA-z]{2,3}$", email);
        log.info("[*] -> {}", result);
    }
}
