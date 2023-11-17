package com.example.banksample.config;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


/**
 * {@code @AutoConfigureMockMvc} 를 사용해야 {@code MockMvc} 를 주입받아서 사용할 수 있다.
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("인증 실패 케이스")
    void authenticate_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(get("/api/test/hello"));
        String body = resultActions.andReturn().getResponse().getContentAsString();
        int status = resultActions.andReturn().getResponse().getStatus();
        log.info("body -> {}", body);
        // then
        // 401 에러가 발생한 건지 비교
        Assertions.assertThat(status).isEqualTo(401);
    }

    @Test
    @DisplayName("인증 실패 케이스, 권한 부족")
    void authorize_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(get("/api/admin/hello"));
        String body = resultActions.andReturn().getResponse().getContentAsString();
        int status = resultActions.andReturn().getResponse().getStatus();
        log.info("body -> {}", body);
        // then
        Assertions.assertThat(status).isEqualTo(401);
    }

}
