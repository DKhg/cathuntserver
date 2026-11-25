package com.hong.cathuntserver.master;

import com.hong.cathuntserver.master.dto.MasterResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 기준 데이터 조회. 공통 데이터라 토큰 없이도 받을 수 있다(앱 시작 때 호출).
@RestController
public class MasterController {

    private final MasterService master;

    // 서비스를 주입받는다.
    public MasterController(MasterService master) {
        this.master = master;
    }

    // GET /master 요청을 처리한다. 토큰이 필요 없는 공통 데이터(뽑기 수치 + 고양이 카탈로그)를 돌려준다.
    @GetMapping("/master")
    public MasterResponse master() {
        return master.load();
    }
}
