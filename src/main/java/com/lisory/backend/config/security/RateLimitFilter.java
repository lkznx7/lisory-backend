package com.lisory.backend.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (method.equals("POST") && path.equals("/auth/login")) {
            String ip = getClientIP(request);
            Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> createLoginBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Muitas tentativas de login. Tente novamente mais tarde.\"}");
                return;
            }
        }

        if (method.equals("POST") && path.equals("/auth/register")) {
            String ip = getClientIP(request);
            Bucket bucket = registerBuckets.computeIfAbsent(ip, k -> createRegisterBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Muitas tentativas de cadastro. Tente novamente mais tarde.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createLoginBucket() {
        int max = properties.login().maxAttempts();
        int duration = properties.login().durationMinutes();
        Refill refill = Refill.intervally(max, Duration.ofMinutes(duration));
        Bandwidth limit = Bandwidth.classic(max, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createRegisterBucket() {
        int max = properties.register().maxAttempts();
        int duration = properties.register().durationMinutes();
        Refill refill = Refill.intervally(max, Duration.ofMinutes(duration));
        Bandwidth limit = Bandwidth.classic(max, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
