package com.lisory.backend.envios.melhorenvio.controller;

import com.lisory.backend.envios.melhorenvio.dto.MelhorEnvioTokenResponse;
import com.lisory.backend.envios.melhorenvio.exception.MelhorEnvioException;
import com.lisory.backend.envios.melhorenvio.service.MelhorEnvioOAuthService;
import com.lisory.backend.shared.log.StructuredLogger;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/melhor-envio/oauth")
public class MelhorEnvioOAuthController {

    private static final StructuredLogger log = StructuredLogger.forClass(MelhorEnvioOAuthController.class);

    private final MelhorEnvioOAuthService oAuthService;

    public MelhorEnvioOAuthController(MelhorEnvioOAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String authorizationUrl = oAuthService.buildAuthorizationUrl(state);

        log.info("oauth_login_redirect", Map.of("state", state));

        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        log.info("oauth_callback_received", Map.of(
                "hasCode", code != null && !code.isBlank(),
                "state", state != null ? state : "none"
        ));

        if (code == null || code.isBlank()) {
            log.warn("oauth_callback_no_code", Map.of());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Authorization code not provided"
            ));
        }

        try {
            MelhorEnvioTokenResponse tokenResponse = oAuthService.exchangeCodeForToken(code);

            log.info("oauth_callback_success", Map.of(
                    "tokenType", tokenResponse.tokenType() != null ? tokenResponse.tokenType() : "null",
                    "expiresIn", String.valueOf(tokenResponse.expiresIn())
            ));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Authorization successful. Tokens saved.",
                    "tokenType", tokenResponse.tokenType() != null ? tokenResponse.tokenType() : "Bearer",
                    "expiresIn", tokenResponse.expiresIn()
            ));
        } catch (MelhorEnvioException e) {
            log.error("oauth_callback_exchange_error", Map.of("errorCode", e.getErrorCode()), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("oauth_callback_unexpected_error", Map.of(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Internal error during authorization"
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean valid = oAuthService.isTokenValid();
        boolean hasTokens = oAuthService.hasTokens();

        return ResponseEntity.ok(Map.of(
                "authenticated", valid,
                "hasTokens", hasTokens
        ));
    }
}
