package com.example.banksample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class BankSampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(BankSampleApplication.class, args);
//        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
//        for (String beanDefinitionName : beanDefinitionNames) {
//            log.info("[디버그] [{}] 이 빈으로 등록되었습니다.", beanDefinitionName);
//        }
    }

}
