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

    // TEMPORÁRIO - TESTES MELHOR ENVIO
    // REMOVER ANTES DE PRODUÇÃO
    private static final String HARDCODED_ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMTE2ZWNjNDc0YjI5MWY5MjhhMGQ0NTk3OTEwZjM3OTdiMDkyYTYzYTU4Y2Y3N2Y4MGNkMmU4MjgwZjFlMjZiMGFiYzNkMGU5NzcxNzA3MDQiLCJpYXQiOjE3ODQwMzE3MDQuMDM5NjQ0LCJuYmYiOjE3ODQwMzE3MDQuMDM5NjQ2LCJleHAiOjE4MTU1Njc3MDQuMDIzMzEsInN1YiI6ImJjYTgwODA3LTExZmItNDFjNC1iZTU5LThmNjVjMDVmODNhMSIsInNjb3BlcyI6WyJjYXJ0LXJlYWQiLCJjYXJ0LXdyaXRlIiwiY29tcGFuaWVzLXJlYWQiLCJjb21wYW5pZXMtd3JpdGUiLCJjb3Vwb25zLXJlYWQiLCJjb3Vwb25zLXdyaXRlIiwibm90aWZpY2F0aW9ucy1yZWFkIiwib3JkZXJzLXJlYWQiLCJwcm9kdWN0cy1yZWFkIiwicHJvZHVjdHMtZGVzdHJveSIsInByb2R1Y3RzLXdyaXRlIiwicHVyY2hhc2VzLXJlYWQiLCJzaGlwcGluZy1jYWxjdWxhdGUiLCJzaGlwcGluZy1jYW5jZWwiLCJzaGlwcGluZy1jaGVja291dCIsInNoaXBwaW5nLWNvbXBhbmllcyIsInNoaXBwaW5nLWdlbmVyYXRlIiwic2hpcHBpbmctcHJldmlldyIsInNoaXBwaW5nLXByaW50Iiwic2hpcHBpbmctc2hhcmUiLCJzaGlwcGluZy10cmFja2luZyIsImVjb21tZXJjZS1zaGlwcGluZyIsInRyYW5zYWN0aW9ucy1yZWFkIiwidXNlcnMtcmVhZCIsInVzZXJzLXdyaXRlIiwid2ViaG9va3MtcmVhZCIsIndlYmhvb2tzLXdyaXRlIiwid2ViaG9va3MtZGVsZXRlIiwidGRlYWxlci13ZWJob29rIl19.OTeWUQXnFsPby887Ugz8j0H9wMIiaGWu9itU1OJ8gxAqMs9up1UsOjisOqnNkrwQaUAtysQkAUZqVY_z_a-L2pd8XLgj8m7RyL-wvyOoK8uLJvsGnzxARy52Xq88WED2MRa8S_nRMDX_Eb7AreqjEk3jhD3z79KjuNRPkEpsqse9reMkYAAx4-lfzi8mSsPI4tYE09rDpCIozmqEgwkXm_1lTMHMBT7gWPBiRSqyew_cSGIcuNJOGWJTAKXSWR-jp-XerPGIoDyRyNRC_-2FgY3psr_dz8OtXul72FUhGv0RMr6zNPr_OD_eQkK80Qyi82ktakcYQrRArimBGXRBTssyJ7lrrY4EK3RLLy6GQms1cZdCbglh52XZBWIvdc2g7a6qXT89fX2kNGeiEHbLm0GfzVU0xT4SmFiW-OKOWxNaQcdDLc3HyRO42ZnbO5kefbdZ1jD_aUtMI1sqj8UZKvU0gCDNNEZC5MD7Xwq-aN4vhXgPYSDNDXMiKDfuaONaeSATeOMG0qNN9hfmbjdEq81h4ZwYg0w-lLkVppICXlnX4tBX1Oe506PvbAf4_lgy4OrAoBIpZLsBnMAslIcb-a-aoc0bDMuCC2q8u_emWvJzNywh5MBoDRU4mCXgjG-vmKpOpBcTCx6MVZA9qcEWQmJmeOq6ZUwTxZimkjk2XXw";
    // FIM TEMPORÁRIO

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
                    properties.apiUrl() + "/oauth/token",
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
        // TEMPORÁRIO - TESTES MELHOR ENVIO
        // REMOVER ANTES DE PRODUÇÃO
        log.debug("Using hardcoded access token for testing");
        return HARDCODED_ACCESS_TOKEN;
        // FIM TEMPORÁRIO
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
                    properties.apiUrl() + "/oauth/token",
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
        // TEMPORÁRIO - TESTES MELHOR ENVIO
        // REMOVER ANTES DE PRODUÇÃO
        log.debug("Hardcoded token considered valid for testing");
        return true;
        // FIM TEMPORÁRIO
    }

    // TEMPORÁRIO - TESTES MELHOR ENVIO
    // REMOVER ANTES DE PRODUÇÃO
    @SuppressWarnings("unused")
    // FIM TEMPORÁRIO
    private boolean isTokenValid(MelhorEnvioCredential credential) {
        // TEMPORÁRIO - TESTES MELHOR ENVIO
        // REMOVER ANTES DE PRODUÇÃO
        return true;
        // FIM TEMPORÁRIO
    }

    public boolean hasTokens() {
        // TEMPORÁRIO - TESTES MELHOR ENVIO
        // REMOVER ANTES DE PRODUÇÃO
        log.debug("Hardcoded token available for testing");
        return true;
        // FIM TEMPORÁRIO
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
