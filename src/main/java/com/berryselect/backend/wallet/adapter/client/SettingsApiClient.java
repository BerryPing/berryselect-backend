package com.berryselect.backend.wallet.adapter.client;

import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SettingsApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${berryselect.app.base-url}")
    private String baseUrl;

    public Mono<UserSettingsResponse> getUserSettings() {
        String authHeader = resolveAuthorizationHeader();

        WebClient client = webClientBuilder.build();
        WebClient.RequestHeadersSpec<?> spec = client.get()
                .uri(baseUrl + "/users/me/settings");

        if (authHeader != null && !authHeader.isBlank()) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, authHeader);
        }

        return spec.retrieve()
                .bodyToMono(UserSettingsResponse.class);
    }

    private String resolveAuthorizationHeader() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}