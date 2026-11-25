package com.hong.cathuntserver.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

// 로그인 토큰(JWT)을 만들고 검증한다. 세션 테이블 없이 토큰 안에 accountId와 만료시각을 담는다.
@Service
public class TokenService {

    private final SecretKey key;
    private final long expireMinutes;

    // 비밀키 문자열(application.yaml의 app.jwt.secret)로 서명용 키를 만들고, 만료 시간(분)을 받아 둔다.
    // 이 비밀키로 서명하므로, 서버만 올바른 토큰을 만들 수 있다(위조 방지).
    public TokenService(@Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.expire-minutes}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMinutes = expireMinutes;
    }

    // accountId를 담은 로그인 토큰을 만든다.
    public String issue(long accountId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(accountId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
            .signWith(key)
            .compact();
    }

    // 토큰에서 accountId를 꺼낸다(만료·위조면 예외). 이후 인증 필터에서 쓴다.
    public long accountIdOf(String token) {
        var claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }
}
