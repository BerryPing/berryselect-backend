package com.berryselect.backend.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-exp}")
    private long accessTokenExpMs;

    @Value("${jwt.refresh-token-exp}")
    private long refreshTokenExpMs;

    @Value("${jwt.issuer:berryselect_backend}")
    private String issuer;

    private Key key;
    private JwtParser parser;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build();
    }

    // createAccessToken -> 로그인 성공 시 JWT 발급
    // GrantedAuthority 형태로 받은 권한도 지원
    public String createAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = (authorities == null)
                ? List.of("ROLE_USER")
                : authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return createAccessToken(subject, roles);
    }

    public String createAccessToken(String subject, List<String> roles){
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .setIssuer(issuer)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createTempAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {

        long tempExpMs = 5 * 60 * 1000L;

        List<String> roles = (authorities == null)
                ? List.of("ROLE_GUEST")
                : authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        Date now = new Date();
        Date exp = new Date(now.getTime() + tempExpMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .setIssuer(issuer)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String subject){
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenExpMs);
        String jti = java.util.UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(subject) // sub: 사용자 식별자
                .setId(jti) // jti: 토큰 고유 ID (회전/블랙리스트용)
                .claim("token_type", "refresh")  // 구분 클레임
                .setIssuedAt(now)
                .setExpiration(exp)
                .setIssuer(issuer)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseClaims(token).get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    // 요청 때 JWT 검증/파싱
    public Claims parseClaims(String token){
        return parser.parseClaimsJws(token).getBody();
    }

    public boolean validate(String token){
        try{
            parseClaims(token);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public String getSubject(String token){
        return parseClaims(token).getSubject();
    }

    public List<String> getRoles(String token){
        Object v = parseClaims(token).get("roles");
        if(v instanceof List<?> list){
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }
}
