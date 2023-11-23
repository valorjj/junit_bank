package com.example.banksample.temp;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class JasyptTest {

    @Test
    void encrypt() {
        log.debug("[google client-id] before: {}, after: {}", "459315597700-q2ju09ub3t2oi5e9cv8uv84863foplfg.apps.googleusercontent.com", jasyptEncoding("459315597700-q2ju09ub3t2oi5e9cv8uv84863foplfg.apps.googleusercontent.com"));
        log.debug("[google client-secret] before: {}, after: {}", "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", jasyptEncoding("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"));
        log.debug("[google redirect-uri] before: {}, after: {}", "http://localhost:3000/oauth/redirected/google", jasyptEncoding("http://localhost:3000/oauth/redirected/google"));
        log.debug("[kakao client-id] before: {}, after: {}", "oauth2-client", jasyptEncoding("oauth2-client"));
        log.debug("[kakao client-secret] before: {}, after: {}", "i9FZxsYjSSVFNj7pEtYjj5l5uu0sIIvR", jasyptEncoding("i9FZxsYjSSVFNj7pEtYjj5l5uu0sIIvR"));
    }

    public String jasyptEncoding(String value) {
        String key = "this-is-my-secret-keep-it-secret";
        StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
        pbeEnc.setAlgorithm("PBEWithMD5AndDES");
        pbeEnc.setPassword(key);
        return pbeEnc.encrypt(value);
    }

}
