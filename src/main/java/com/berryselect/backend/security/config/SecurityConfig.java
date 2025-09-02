package com.berryselect.backend.security.config;

import com.berryselect.backend.security.filter.JwtAuthenticationFilter;
import com.berryselect.backend.security.handler.RestAccessDeniedHandler;
import com.berryselect.backend.security.handler.RestAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; //

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, RestAuthEntryPoint restAuthEntryPoint, RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // 세션-폼로그인 사용 안하므로 CSRF off
                .cors(Customizer.withDefaults())  // ← CORS 활성화
                // STATELESS (JWT) 매 요청 JWT로 인증
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 폼로그인/HTTP Basic 비활성화 (JWT만 사용)
                .formLogin(f1-> f1.disable())
                .httpBasic(hb -> hb.disable())
                // 인가규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**","/actuator/health").permitAll()
                        .anyRequest().authenticated() // 나머지는 JWT 필요, 토큰 없으면 401
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173" // 프론트 개발 서버 도메인/포트
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
