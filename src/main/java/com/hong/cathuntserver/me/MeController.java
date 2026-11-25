package com.hong.cathuntserver.me;

import com.hong.cathuntserver.auth.CurrentAccountId;
import com.hong.cathuntserver.me.dto.MeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 내 데이터 조회. 토큰 필요(Authorization: Bearer). 토큰이 없거나 틀리면 401.
@RestController
public class MeController {

    private final MeService me;

    // 서비스를 주입받는다.
    public MeController(MeService me) {
        this.me = me;
    }

    // GET /me 요청을 처리한다. @CurrentAccountId가 토큰에서 꺼낸 내 계정 ID를 넣어 주고(없으면 401),
    // 그 ID로 내 전체 데이터를 모아 돌려준다.
    @GetMapping("/me")
    public MeResponse me(@CurrentAccountId long accountId) {
        return me.load(accountId);
    }
}
