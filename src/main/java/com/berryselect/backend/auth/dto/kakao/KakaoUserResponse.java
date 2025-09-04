package com.berryselect.backend.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserResponse {

    private Long id; // 카카오 고유 id

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        private String email;

        @JsonProperty("phone_number")
        private String phoneNumber;

        @JsonProperty("birthyear")
        private String birthyear;

        @JsonProperty("birthday")
        private String birthday;

        private Profile profile;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
        }
    }
}
