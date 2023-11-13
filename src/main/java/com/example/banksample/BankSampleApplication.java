package com.example.banksample;

import com.example.banksample.config.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@PropertySource(value = {
  "classpath:env/env.yml"
  }, factory = EnvConfig.class)
public class BankSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankSampleApplication.class, args);
    }

}
