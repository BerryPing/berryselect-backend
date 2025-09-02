package com.berryselect.backend.security.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 비밀번호 저장/검증에 사용할 BCrypt 해시 인코더
        return new BCryptPasswordEncoder();
    }
}
