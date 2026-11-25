package com.hong.cathuntserver.account;

import com.hong.cathuntserver.auth.TokenService;
import com.hong.cathuntserver.auth.dto.GuestLoginRequest;
import com.hong.cathuntserver.auth.dto.GuestLoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 게스트 로그인: 기기 키가 처음이면 새 계정(+지갑+시작 고양이), 있으면 찾아서 토큰을 돌려준다(PRD 9.1).
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GuestLoginService guestLogin;
    private final TokenService tokens;

    // 스프링이 시작할 때 필요한 부품(서비스·토큰)을 자동으로 넣어 준다(생성자 주입).
    public AuthController(GuestLoginService guestLogin, TokenService tokens) {
        this.guestLogin = guestLogin;
        this.tokens = tokens;
    }

    // POST /auth/guest 요청을 처리한다. 본문의 기기 키로 로그인(없으면 새 계정)한 뒤,
    // 그 계정 ID로 토큰을 만들어 {계정ID, 토큰, 새 계정인지}를 돌려준다.
    // @Valid: 요청 본문이 규칙(기기 키 빈 값 금지 등)을 어기면 자동으로 400을 낸다.
    @PostMapping("/guest")
    public GuestLoginResponse guest(@Valid @RequestBody GuestLoginRequest req) {
        var result = guestLogin.login(req.deviceKey());
        String token = tokens.issue(result.account().getId());
        return new GuestLoginResponse(result.account().getId(), token, result.isNew());
    }
}
