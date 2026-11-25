package com.hong.cathuntserver.gacha;

import com.hong.cathuntserver.auth.CurrentAccountId;
import com.hong.cathuntserver.gacha.dto.GachaDrawRequest;
import com.hong.cathuntserver.gacha.dto.GachaDrawResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 뽑기. 토큰 필요(Authorization: Bearer). 서버가 비용·확률·중복환급을 계산해 결과만 돌려준다.
@RestController
@RequestMapping("/gacha")
public class GachaController {

    private final GachaService gacha;

    // 서비스를 주입받는다.
    public GachaController(GachaService gacha) {
        this.gacha = gacha;
    }

    // POST /gacha/draw — 내 계정으로 뽑기 1회. requestKey로 재전송을 멱등 처리한다.
    @PostMapping("/draw")
    public GachaDrawResponse draw(@CurrentAccountId long accountId, @Valid @RequestBody GachaDrawRequest req) {
        return gacha.draw(accountId, req.requestKey());
    }
}
