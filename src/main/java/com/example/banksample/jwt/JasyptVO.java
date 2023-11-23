package com.example.banksample.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JasyptVO {

    public static String JASYPT_SECRET_KEY;

    @Value("${jasypt.encryptor.key}")
    public void setJasyptKey(String JASYPT_SECRET_KEY) {
        this.JASYPT_SECRET_KEY = JASYPT_SECRET_KEY;
    }


}
