package com.berryselect.backend.auth.service;

import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.dto.kakao.KakaoTokenResponse;
import com.berryselect.backend.auth.dto.kakao.KakaoUserResponse;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOauthClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // code로 카카오 토큰 교환 -> 유저 조회 -> 우리 DB upsert -> 우리 JWT발급
    @Transactional
    public AuthResult loginWithAuthorizationCode(String code){
        //code -> Kakao token
        KakaoTokenResponse token = kakaoClient.exchangeCodeForToken(code);

        // kakao access_token으로 유저 정보
        KakaoUserResponse me = kakaoClient.getUserMe(token.getAccessToken());
        Long kakaoId = me.getId();

        // DB upsert
        User user = userRepository
                .findByProviderAndProviderUserId(User.Provider.KAKAO, String.valueOf(kakaoId))
                .orElseGet(()-> User.builder()
                        .provider(User.Provider.KAKAO)
                        .providerUserId(String.valueOf(kakaoId))
                        .build());

        // 이름 : nickname 우선
        String nickname = (me.getKakaoAccount() != null
        && me.getKakaoAccount().getProfile() != null)
                ? me.getKakaoAccount().getProfile().getNickname()
                : null;

        // 전화번호 : 그대로 저장
        String phone = (me.getKakaoAccount() != null) ? me.getKakaoAccount().getPhoneNumber() : null;

        // 생년월일 : birthyear(YYYY) + birthday(MMDD) -> LocalDate
        LocalDate birth = null;
        if(me.getKakaoAccount() != null){
            String by = safe(me.getKakaoAccount().getBirthyear()); // "1996"
            String bd = safe(me.getKakaoAccount().getBirthday()); // "0923"
            if(by.length() == 4 && bd.length() == 4){
                int year = Integer.parseInt(by);
                int month = Integer.parseInt(bd.substring(0,2));
                int day = Integer.parseInt(bd.substring(2,4));
                birth = LocalDate.of(year, month, day);
            }
        }

        user.setName(nickname);
        user.setPhone(phone);
        user.setBirth(birth);
        user.setAccessToken(token.getAccessToken());
        user.setRefreshToken(token.getRefreshToken());
        if(token.getExpiresIn() != null){
            user.setTokenExpiresAt(Instan.now().plusSeconds(token.getExpiresIn()));
        }
        userRepository.save(user);

        // 우리 서비스 JWT 발급
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String accessJwt = jwtProvider.createAccessToken(String.valueOf(user.getId()), authorities);
        String refreshJwt = jwtProvider.createRefreshToken(String.valueOf(user.getId()));

        return new AuthResult(user.getId(), user.getName(), accessJwt, refreshJwt, "Bearer");
    }

    private static String safe(String v) {return v == null ? "" : v;}

    public record AuthResult(Long userId, String name, String accessToken, String refreshToken, String tokenType){}

}
