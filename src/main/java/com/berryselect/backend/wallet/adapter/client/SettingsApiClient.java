package com.berryselect.backend.wallet.adapter.client;

import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SettingsApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${berryselect.app.base-url}")
    private String baseUrl; // application.properties에서 읽어옴

    public Mono<UserSettingsResponse> getUserSettings(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/users/me/settings")
                .header("X-User-Id", String.valueOf(userId)) // Security 연동 전
                .retrieve()
                .bodyToMono(UserSettingsResponse.class);
    }
}