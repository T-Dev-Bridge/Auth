package com.web.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.TimeZone;

@Slf4j
@EnableFeignClients(basePackages = "com.web.auth.client") // @FeignClient 어노테이션이 붙은 인터페이스를 스캔하여 빈으로 등록한다.
@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        log.info("Spring boot application running in Asia/Seoul timezone : " + new Date());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}