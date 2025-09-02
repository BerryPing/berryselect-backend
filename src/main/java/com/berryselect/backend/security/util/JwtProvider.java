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

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token){
        Object v = parseClaims(token).get("roles");
        if(v instanceof List<?> list){
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }
}
