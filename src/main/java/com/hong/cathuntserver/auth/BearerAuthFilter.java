package com.hong.cathuntserver.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 모든 요청에서 Authorization: Bearer <token>을 읽어, 유효하면 accountId를 request 속성에 담는다.
// 막지는 않는다(토큰 없는 /auth/guest도 통과). 토큰이 필요한 곳은 CurrentAccountId가 없으면 401을 낸다.
@Component
public class BearerAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_ACCOUNT_ID = "accountId";
    private static final String PREFIX = "Bearer ";

    private final TokenService tokens;

    // 토큰 검증기를 주입받는다.
    public BearerAuthFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            try {
                long accountId = tokens.accountIdOf(token);  // 만료·위조면 예외
                request.setAttribute(ATTR_ACCOUNT_ID, accountId);
            } catch (Exception ignored) {
                // 토큰이 잘못됐으면 인증 안 된 것으로 둔다(속성 미설정 → 보호된 엔드포인트에서 401)
            }
        }
        chain.doFilter(request, response);
    }
}
