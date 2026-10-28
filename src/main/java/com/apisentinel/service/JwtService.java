package com.apisentinel.service;

import com.apisentinel.config.JwtProperties;
import com.apisentinel.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String ISSUER = "api-sentinel";

    public record IssuedToken(String value, long expiresInSeconds) {
    }

    private final SecretKey key;
    private final JwtProperties props;
    private final Clock clock;

    public JwtService(JwtProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issue(User user) {
        Instant now = Instant.now(clock);
        Instant expiry = now.plus(Duration.ofMinutes(props.expiryMinutes()));

        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return new IssuedToken(token, props.expiryMinutes() * 60);
    }
}