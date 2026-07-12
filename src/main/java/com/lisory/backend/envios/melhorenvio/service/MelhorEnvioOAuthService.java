package com.lisory.backend.envios.melhorenvio.service;

import com.lisory.backend.config.properties.MelhorEnvioProperties;
import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioTokenResponse;
import com.lisory.backend.envios.melhorenvio.entity.MelhorEnvioCredential;
import com.lisory.backend.envios.melhorenvio.exception.MelhorEnvioException;
import com.lisory.backend.envios.melhorenvio.repository.MelhorEnvioCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class MelhorEnvioOAuthService {

    private static final Logger log = LoggerFactory.getLogger(MelhorEnvioOAuthService.class);

    private final RestTemplate restTemplate;
    private final MelhorEnvioProperties properties;
    private final MelhorEnvioCredentialRepository credentialRepository;

    public MelhorEnvioOAuthService(
            RestTemplate defaultRestTemplate,
            MelhorEnvioProperties properties,
            MelhorEnvioCredentialRepository credentialRepository
    ) {
        this.restTemplate = defaultRestTemplate;
        this.properties = properties;
        this.credentialRepository = credentialRepository;
    }

    public String buildAuthorizationUrl(String state) {
        String scopes = properties.scopes() != null && !properties.scopes().isBlank()
                ? properties.scopes()
                : "cart-read cart-write orders-read shipping-calculate shipping-cancel shipping-checkout shipping-companies shipping-generate shipping-preview shipping-print shipping-share shipping-tracking";

        return properties.apiUrl() + "/oauth/authorize"
                + "?client_id=" + URLEncoder.encode(properties.clientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(properties.callbackUrl(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8);
    }

    public synchronized MelhorEnvioTokenResponse exchangeCodeForToken(String code) {
        log.info("Exchanging authorization code for access token...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.clientId());
        body.add("client_secret", properties.clientSecret());
        body.add("redirect_uri", properties.callbackUrl());
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<MelhorEnvioTokenResponse> response = restTemplate.exchange(
                    properties.apiUrl() + "/auth/token",
                    HttpMethod.POST,
                    request,
                    MelhorEnvioTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MelhorEnvioTokenResponse tokenResponse = response.getBody();
                saveTokens(tokenResponse.accessToken(), tokenResponse.refreshToken(), tokenResponse.expiresIn());
                log.info("Authorization code exchanged successfully, token expires in {} seconds", tokenResponse.expiresIn());
                return tokenResponse;
            } else {
                log.error("Failed to exchange code: unexpected status code {}", response.getStatusCode());
                throw new MelhorEnvioException(
                        "Failed to exchange authorization code: " + response.getStatusCode(),
                        "CODE_EXCHANGE_FAILED"
                );
            }
        } catch (MelhorEnvioException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error exchanging authorization code", e);
            throw new MelhorEnvioException("Error exchanging authorization code: " + e.getMessage(), e);
        }
    }

    public synchronized String getAccessToken() {
        MelhorEnvioCredential credential = loadCredential();
        if (credential == null || !isTokenValid(credential)) {
            log.info("Access token is missing or expired, refreshing...");
            refreshToken();
            credential = loadCredential();
        }
        return credential != null ? credential.getAccessToken() : null;
    }

    public synchronized void refreshToken() {
        MelhorEnvioCredential credential = loadCredential();
        if (credential == null || credential.getRefreshToken() == null || credential.getRefreshToken().isBlank()) {
            log.warn("No refresh token available. User must authorize via /oauth/login first.");
            throw new MelhorEnvioException("No refresh token available. Authorize first.", "NO_REFRESH_TOKEN");
        }

        log.info("Requesting new access token from Melhor Envio...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", properties.clientId());
        body.add("client_secret", properties.clientSecret());
        body.add("refresh_token", credential.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<MelhorEnvioTokenResponse> response = restTemplate.exchange(
                    properties.apiUrl() + "/auth/token",
                    HttpMethod.POST,
                    request,
                    MelhorEnvioTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MelhorEnvioTokenResponse tokenResponse = response.getBody();
                saveTokens(tokenResponse.accessToken(), tokenResponse.refreshToken(), tokenResponse.expiresIn());
                log.info("Access token refreshed successfully, expires in {} seconds", tokenResponse.expiresIn());
            } else {
                log.error("Failed to refresh token: unexpected status code {}", response.getStatusCode());
                throw new MelhorEnvioException(
                        "Failed to refresh Melhor Envio token: " + response.getStatusCode(),
                        "TOKEN_REFRESH_FAILED"
                );
            }
        } catch (MelhorEnvioException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing Melhor Envio token", e);
            throw new MelhorEnvioException("Error refreshing Melhor Envio token: " + e.getMessage(), e);
        }
    }

    public boolean isTokenValid() {
        MelhorEnvioCredential credential = loadCredential();
        return credential != null && isTokenValid(credential);
    }

    private boolean isTokenValid(MelhorEnvioCredential credential) {
        return credential.getAccessToken() != null
                && !credential.getAccessToken().isBlank()
                && credential.getTokenExpiresAt() != null
                && Instant.now().isBefore(credential.getTokenExpiresAt());
    }

    public boolean hasTokens() {
        MelhorEnvioCredential credential = loadCredential();
        return credential != null && credential.getRefreshToken() != null && !credential.getRefreshToken().isBlank();
    }

    @Scheduled(fixedDelayString = "${melhor-envio.token-refresh-interval:1800000}")
    public void scheduledTokenRefresh() {
        if (!hasTokens()) {
            log.debug("Scheduled token refresh: no tokens stored yet, waiting for OAuth authorization");
            return;
        }
        if (!isTokenValid()) {
            log.info("Scheduled token refresh triggered");
            try {
                refreshToken();
            } catch (MelhorEnvioException e) {
                log.warn("Scheduled token refresh failed: {}", e.getMessage());
            }
        } else {
            log.debug("Scheduled token refresh check: token still valid");
        }
    }

    private MelhorEnvioCredential loadCredential() {
        return credentialRepository.findFirstByOrderByIdDesc().orElse(null);
    }

    private void saveTokens(String accessToken, String refreshToken, int expiresIn) {
        MelhorEnvioCredential credential = loadCredential();
        if (credential == null) {
            credential = new MelhorEnvioCredential();
        }
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        credential.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn - 60));
        credentialRepository.save(credential);
    }
}
