package com.berryselect.backend.security.filter;

import com.berryselect.backend.security.util.JwtProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// 매 요청마다 Authorization 헤더의 Bearer 토큰을 검증하고,
// 유효하면 SecurityContext에 인증 정보를 세팅한다.

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");

        if(auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try{
                // 1. 토큰 검증 (서명/만료/issuer 등)
                if(jwtProvider.validate(token)){
                    // 2. 클레임에서 사용자/권한 꺼내기
                    String subject = jwtProvider.getSubject(token);
                    List<String> roles = jwtProvider.getRoles(token);

                    // 3. 권한 변환 (roles(List<String>) -> GrantedAuthority 컬렉션으로 변환 = 타입 변환)
                    var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 4. 인증 객체 생성 ( 비밀번호는 필요 없음 )
                    var authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(req)
                    );

                    // 5. 인증 정보를 스프링 컨텍스트에 주입 -> 컨트롤러 단에서 "인증된 사용자"로 동작
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            catch(JwtException | IllegalArgumentException e){
                // 토큰 문제면 인증 정보 세팅하지 않고 넘김 -> 보호 차원 접근 시 401/403은 핸들러가 처리
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
