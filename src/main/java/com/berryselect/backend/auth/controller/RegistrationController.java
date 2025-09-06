package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.domain.*;
import com.berryselect.backend.auth.dto.kakao.KakaoUserResponse;
import com.berryselect.backend.auth.dto.response.AuthResult;
import com.berryselect.backend.auth.repository.*;
import com.berryselect.backend.auth.service.KakaoOauthClient;
import com.berryselect.backend.auth.service.OnboardingTokenStore;
import com.berryselect.backend.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegistrationController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoOauthClient kakaoClient;
    private final OnboardingTokenStore onboardingTokenStore;

    private final UserSettingsRepository userSettingsRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Value("${jwt.refresh-token-exp}")
    private long refreshTokenExpMs;

    private static final String CONSENT_VERSION = "v1.0"; // 약관 버전 관리

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<AuthResult> register(
            HttpServletRequest request,
            @RequestBody RegisterRequest req
            ){

        // 1) 임시 게스트 토큰에서 kakaoId 추출
        String auth = request.getHeader("Authorization");
        if(auth == null || !auth.startsWith("Bearer ")){
            return ResponseEntity.status(401).build();
        }
        String sub = jwtProvider.getSubject(auth.substring(7));
        if (sub == null || !sub.startsWith("guest:"))   return ResponseEntity.status(401).build();
        String kakaoId = sub.substring("guest:".length());


        // 2) 이미 가입된 사용자라면 409
        if(userRepository.findByProviderAndProviderUserId(User.Provider.KAKAO, kakaoId).isPresent()){
            return ResponseEntity.status(409).build();
        }

        // 3) 요청 검증 (필수 동의 & 카테고리 최대 3개)
        if (!(req.isAgreeTerms() && req.isAgreePrivacy() && req.isAgreeKakaoAlert() && req.isAgreeDataUsage())) {
            return ResponseEntity.badRequest().build();
        }
        if (req.getCategories() != null && req.getCategories().size() > 3) {
            return ResponseEntity.badRequest().build();
        }

        // 4) 임시 보관소에서 카카오 access_token 회수
        String kakaoAccessToken = onboardingTokenStore.consume(kakaoId);
        if (kakaoAccessToken == null) return ResponseEntity.status(401).build();


        // 5) 카카오에서 프로필 조회 (이름/전화/생일)
        KakaoUserResponse me = kakaoClient.getUserMe(kakaoAccessToken);
        String name = (me.getKakaoAccount() != null && me.getKakaoAccount().getProfile() != null)
                ? me.getKakaoAccount().getProfile().getNickname() : null;
        String phone = (me.getKakaoAccount() != null) ? me.getKakaoAccount().getPhoneNumber() : null;
        LocalDate birth = null;
        if (me.getKakaoAccount() != null) {
            String by = safe(me.getKakaoAccount().getBirthyear());
            String bd = safe(me.getKakaoAccount().getBirthday());
            if (by.length() == 4 && bd.length() == 4) {
                birth = LocalDate.of(Integer.parseInt(by), Integer.parseInt(bd.substring(0, 2)), Integer.parseInt(bd.substring(2, 4)));
            }
        }



        // 6) users INSERT
        User user = User.builder()
                .provider(User.Provider.KAKAO)
                .providerUserId(kakaoId)
                .name(name)
                .phone(phone)
                .birth(birth)
                .build();
        userRepository.save(user);

        // 7) user_setting UPSERT
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setAllowKakaoAlert(req.isAgreeKakaoAlert());
        settings.setMarketingOption(req.isMarketingOptIn());
        settings.setNotifyBudgetAlert(req.isWarnOverBudget());
        settings.setNotifyGifticonExpire(req.isGifticonExpireAlert());
        settings.setNotifyBenefitEvents(req.isEventAlert());
        userSettingsRepository.save(settings);

        // 8) user_consents INSERT
        Instant now = Instant.now();
        List<UserConsent> consents = new ArrayList<>();
        consents.add(buildConsent(user, ConsentType.TOS,         CONSENT_VERSION, req.isAgreeTerms(),       now, request));
        consents.add(buildConsent(user, ConsentType.PRIVACY,     CONSENT_VERSION, req.isAgreePrivacy(),     now, request));
        consents.add(buildConsent(user, ConsentType.KAKAO_ALERT, CONSENT_VERSION, req.isAgreeKakaoAlert(),  now, request));
        consents.add(buildConsent(user, ConsentType.MYDATA,      CONSENT_VERSION, req.isAgreeDataUsage(),   now, request));
        consents.add(buildConsent(user, ConsentType.MARKETING,   CONSENT_VERSION, req.isMarketingOptIn(),   now, request));
        userConsentRepository.saveAll(consents);

        // 9) user_preferred_categories INSERT
        if(req.getCategories() != null){
            for(String nameCat : req.getCategories()){
                categoryRepository.findByName(nameCat).ifPresent(cat ->{
                   UserPreferredCategory upc = new UserPreferredCategory();
                   upc.setUser(user);
                   upc.setCategory(cat);
                   userPreferredCategoryRepository.save(upc);
                });
            }
        }

        // 10) 정규 토큰 발급 + refresh 영속화
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String accessJwt  = jwtProvider.createAccessToken(String.valueOf(user.getId()), authorities);
        String refreshJwt = jwtProvider.createRefreshToken(String.valueOf(user.getId()));
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user).refreshToken(refreshJwt)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpMs))
                .build());

        return ResponseEntity.ok(AuthResult.builder()
                .accessToken(accessJwt)
                .refreshToken(refreshJwt)
                .isNewUser(false)
                .build());
    }

    private static UserConsent buildConsent(
            User u,
            ConsentType t,
            String ver,
            boolean agreed,
            Instant at,
            HttpServletRequest req
    ) {
        UserConsent c = new UserConsent();
        c.setUser(u);
        c.setConsentType(t);
        c.setVersion(ver);
        c.setAgreed(agreed);
        c.setAgreedAt(at);
        c.setSourceIp(req.getRemoteAddr());
        return c;
    }

    private static String safe(String v) { return v == null ? "" : v; }


    // 요청 DTO
    @Getter @Setter
    public static class RegisterRequest {
        private List<String> categories;     // 최대 3개
        private boolean warnOverBudget;      // 예산 초과 경고
        private boolean gifticonExpireAlert; // 기프티콘 만료 알림
        private boolean eventAlert;          // 혜택 이벤트 알림
        private boolean agreeTerms;          // (필수)
        private boolean agreePrivacy;        // (필수)
        private boolean agreeKakaoAlert;     // (필수)
        private boolean agreeDataUsage;      // (필수)
        private boolean marketingOptIn;      // (선택)
        private String primaryCategory;      // 선택(대표 카테고리)
    }


}
