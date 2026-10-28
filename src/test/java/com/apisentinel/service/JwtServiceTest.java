package com.apisentinel.service;

import com.apisentinel.config.JwtProperties;
import com.apisentinel.entity.User;
import com.apisentinel.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
    private static final String SECRET = "test-secret-test-secret-test-secret-123456";

    private final JwtService service = new JwtService(
            new JwtProperties(SECRET, 60), Clock.fixed(NOW, ZoneOffset.UTC));

    private User user() {
        User user = new User("a@x.com", "hash", Role.USER);
        ReflectionTestUtils.setField(user, "id", 5L);
        return user;
    }

    private SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parse(String token, Instant at) {
        return Jwts.parser().verifyWith(key(SECRET)).clock(() -> Date.from(at)).build()
                .parseSignedClaims(token).getPayload();
    }

    @Test
    @DisplayName("token mein sub, role, iat, exp sahi hon, aur email na ho")
    void tokenContainsExpectedClaims() {
        String token = service.issue(user()).value();

        Claims claims = parse(token, NOW);

        assertThat(claims.getSubject()).isEqualTo("5");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getIssuer()).isEqualTo("api-sentinel");
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plusSeconds(3600));
        assertThat(claims.containsKey("email")).isFalse();
    }

    @Test
    @DisplayName("expiry ke baad token reject")
    void expiredTokenRejected() {
        String token = service.issue(user()).value();

        assertThatThrownBy(() -> parse(token, NOW.plusSeconds(3601)))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("payload badla toh token reject")
    void tamperedPayloadRejected() {
        String[] parts = service.issue(user()).value().split("\\.");
        String tampered = parts[0] + "." + "X" + parts[1].substring(1) + "." + parts[2];

        assertThatThrownBy(() -> parse(tampered, NOW)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("alag secret se verify karne par reject")
    void wrongKeyRejected() {
        String token = service.issue(user()).value();

        assertThatThrownBy(() -> Jwts.parser()
                .verifyWith(key("another-secret-another-secret-another-1234"))
                .clock(() -> Date.from(NOW)).build().parseSignedClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("32 bytes se chhota secret par config hi na bane")
    void shortSecretRejected() {
        assertThatThrownBy(() -> new JwtProperties("too-short", 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining("too-short");
    }
}