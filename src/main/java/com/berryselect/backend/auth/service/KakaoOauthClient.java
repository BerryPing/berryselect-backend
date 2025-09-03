package com.berryselect.backend.auth.service;

import com.berryselect.backend.config.KakaoOauthProperties;
import com.berryselect.backend.auth.dto.kakao.KakaoTokenResponse;
import com.berryselect.backend.auth.dto.kakao.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoOauthClient {

    private final KakaoOauthProperties props;

    // Spring에서 Http요청을 보내는 클라이언트 = 카카오 API에 HTTP 요청을 보내는 도구
    private final WebClient webClient = WebClient.builder().build();

    // 인가 코드 -> 카카오 토큰 교환
    public KakaoTokenResponse exchangeCodeForToken(String code){
        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "grant_type=authorization_code" +
                                "&client_id=" + props.getRestKey()+
                                "&redirect_uri=" + props.getRedirectUri()+
                                "&code="+code
                )
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    // access_token으로 유저 정보 조회
    public KakaoUserResponse getUserMe(String kakaoAccessToken){
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .headers(h->h.setBearerAuth(kakaoAccessToken))
                .retrieve()
                .bodyToMono(KakaoUserResponse.class)
                .block();
    }
}
