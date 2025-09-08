package com.berryselect.backend.notification.service;

import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.common.exception.ApiException;
import com.berryselect.backend.notification.domain.Notification;
import com.berryselect.backend.notification.domain.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoNotificationAdapter {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kakao.message.api.url:https://kapi.kakao.com}")
    private String kakaoApiUrl;

    @Value("${kakao.message.enabled:false}")
    private boolean kakaoMessageEnabled;

    @Value("${berryselect.app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    /**
     * 카카오톡 "나에게 보내기" 메시지 발송
     * ex ) 사용자 A 알림 -> A의 카카오 토큰 -> A의 "나와의 채팅"에 메시지 전송
     * @param notification 발송할 알림
     * @return 발송 결과 코드
     * @throws ApiException 발송 실패 시
     */
    public String sendNotification(Notification notification) throws ApiException {
        if(!kakaoMessageEnabled) {
            log.info("카카오 메시지 API가 비활성화 상태입니다.");
            return "DISABLED";
        }

        try {
            // 해당 사용자의 카카오 토큰 조회
            String userKakaoToken = getUserKakaoToken(notification.getUser().getId());

            if(userKakaoToken == null || userKakaoToken.trim().isEmpty()) {
                log.warn("해당 사용자의 카카오 토큰이 없습니다. - userId: {}", notification.getUser().getId());
                throw new ApiException("KAKAO_TOKEN_MISSING", "사용자의 카카오 엑세스 토큰이 없습니다.");
            }

            log.info("카카오톡 메시지 발송 시작 - notificationId: {}, userId: {}", notification.getId(), notification.getUser().getId());

            // 해당 사용자의 카카오 토큰으로 메시지 발송
            String resultCode = sendKakaoMessageWithUserToken(notification, userKakaoToken);

            log.info("카카오톡 메시지 발송 성공 - notificationId: {}, resultCode: {}", notification.getId(), resultCode);

            return resultCode;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오톡 메시지 발송 실패 - notificationId: {}", notification.getId(), e);
            throw new ApiException("KAKAO_SEND_FAILED", "카카오톡 메시지 발송 실패: " + e.getMessage(), e);
        }
    }

    // 사용자별 카카오 토큰 조회
    private String getUserKakaoToken(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

            // 디버깅 로그 추가
            log.info("=== 토큰 만료 체크 시작 ===");
            log.info("토큰 만료시간: {}", user.getTokenExpiresAt());
            log.info("현재 시간: {}", Instant.now());

            // 토큰 만료 확인
            if(user.getTokenExpiresAt() != null &&
                user.getTokenExpiresAt().isBefore(Instant.now())) {
                log.warn("사용자의 카카오 토큰이 만료됨 - userId: {}", userId);
                // TODO : refresh_token으로 갱신 로직 구현
                throw new ApiException("KAKAO_TOKEN_EXPIRED", "카카오 토큰이 만료되었습니다.");
            }
            return user.getAccessToken();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 카카오 토큰 조회 실패 - userId: {}", userId, e);
            throw new ApiException("TOKEN_FETCH_FAILED", "카카오 토큰 조회에 실패했습니다.");
        }
    }

    // 실제 카카오 메시지 API 호출
    private String sendKakaoMessageWithUserToken(Notification notification, String userToken) throws ApiException {
        String url = kakaoApiUrl + "/v2/api/talk/memo/default/send";

        try {
            // 사용자별 토큰 설정 (HTTP 헤더 설정)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + userToken);

            // 템플릿 객체
            Map<String, Object> templateObject = buildTemplateObject(notification);
            String templateJson = objectMapper.writeValueAsString(templateObject);

            // Form 데이터 구성
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("template_object", templateJson);

            log.debug("카카오 API 요청 URL: {}", url);
            log.debug("카카오 API 템플릿: {}", templateJson);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            // 응답 처리
            if(response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("카카오 API 응답: {}", responseBody);
                return "SUCCESS";
            } else {
                throw new ApiException("KAKAO_API_ERROR", "카카오 API 응답 실패 - HTTP" + response.getStatusCode());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류 발생", e);
            throw new ApiException("KAKAO_API_CALL_FAILED", "카카오 API 호출 실패: " + e.getMessage(), e);
        }
    }

    // 카카오 메시지 템플릿 객체 생성
    private Map<String, Object> buildTemplateObject(Notification notification) {
        Map<String, Object> templateObject = new HashMap<>();

        // 기본 텍스트 메시지 템플릿
        templateObject.put("object_type", "text");
        templateObject.put("text", buildMessageText(notification));

        // 내 알림 조회 페이지로 이동
        Map<String, String> link = new HashMap<>();
        link.put("web_url", appBaseUrl + "/notifications");
        link.put("mobile_web_url", appBaseUrl + "/notifications");
        templateObject.put("link", link);

        return templateObject;
    }

    // 메시지 텍스트 구성
    private String buildMessageText(Notification notification) {
        StringBuilder text = new StringBuilder();

        // 헤더
        text.append("🫐 베리셀렉트 💜\n");
        text.append("--------------\n\n");

        // 알림 타입 이모지 + 제목
        text.append(getTypeEmoji(notification.getNotificationType()));
        text.append(" ").append(notification.getTitle()).append("\n\n");

        // 바디
        text.append(notification.getBody()).append("\n\n");

        // 발송 시간
        text.append("발송 시간: ");
        text.append(LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )).append("\n\n");

        // 푸터
        text.append("💟 베리셀렉트에서 확인하세요!");

        return text.toString();
    }

    // 알림 타입별 이모지
    private String getTypeEmoji(NotificationType type) {
        return switch (type) {
            case BUDGET_ALERT -> "💰";
            case GIFTICON_EXPIRE -> "🎁";
            case BENEFIT_EVENT -> "⭐️";
        };
    }

    // 카카오톡 연결 테스트
    public boolean testConnection(Long testUserId) {
        try {
            log.info("카카오톡 메시지 API 연결 테스트 시작 - testUserId: {}", testUserId);

            if (!kakaoMessageEnabled) {
                log.warn("카카오 메시지가 비활성화되어 있습니다.");
                return false;
            }

            // 테스트 사용자 토큰 조회
            String userToken = getUserKakaoToken(testUserId);

            if(userToken == null || userToken.trim().isEmpty()) {
                log.error("테스트 사용자의 카카오 토큰이 없습니다.");
                return false;
            }

            // 테스트 메시지 발송
            String testMessage = buildTestMessage();
            Map<String, Object> templateObject = new HashMap<>();
            templateObject.put("object_type", "text");
            templateObject.put("text", testMessage);
            templateObject.put("link", Map.of(
                    "web_url", appBaseUrl + "/notifications",
                    "mobile_web_url", appBaseUrl + "/notifications"
            ));

            String templateJson = objectMapper.writeValueAsString(templateObject);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + userToken);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("template_object", templateJson);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            String url = kakaoApiUrl + "/v2/api/talk/memo/default/send";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            boolean success = response.getStatusCode() == HttpStatus.OK;
            if (success) {
                log.info("카카오톡 메시지 API 연결 테스트 성공");
            } else {
                log.error("카카오톡 메시지 API 연결 테스트 실패 - HTTP {}", response.getStatusCode());
            }

            return success;
        } catch (Exception e) {
            log.error("카카오톡 메시지 API 연결 테스트 중 오류 발생", e);
            return false;
        }
    }

    // 테스트 메시지 구성
    private String buildTestMessage() {
        return """
            🔧 베리셀렉트 알림 시스템 테스트
            ━━━━━━━━━━━━━━━━━━━━━
            
            ✅ 카카오톡 메시지 API 연동 성공!
            ✅ 알림 시스템이 정상적으로 작동합니다.
            
            🍓 포트폴리오 프로젝트: 베리셀렉트
            📱 실시간 알림 시스템 데모
            
            🕐 테스트 시간: %s
            """.formatted(LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss")
        ));
    }
}
