package com.berryselect.backend.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResult {

    private final String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
}
