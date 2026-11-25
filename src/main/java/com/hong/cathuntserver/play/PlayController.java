package com.hong.cathuntserver.play;

import com.hong.cathuntserver.auth.CurrentAccountId;
import com.hong.cathuntserver.play.dto.PlayReportRequest;
import com.hong.cathuntserver.play.dto.PlayReportResponse;
import com.hong.cathuntserver.play.dto.PlayStartResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 한 판 시작/보고. 둘 다 토큰 필요. 보상은 서버가 계산한다.
@RestController
@RequestMapping("/play")
public class PlayController {

    private final PlayService play;

    // 서비스를 주입받는다.
    public PlayController(PlayService play) {
        this.play = play;
    }

    // POST /play/start — 한 판 시작. 플레이 ID를 돌려준다(사냥 들어갈 때 호출).
    @PostMapping("/start")
    public PlayStartResponse start(@CurrentAccountId long accountId) {
        return play.start(accountId);
    }

    // POST /play/report — 한 판 결과 보고. 플레이 ID로 보상을 한 번만 반영한다(사냥 끝날 때 호출).
    @PostMapping("/report")
    public PlayReportResponse report(@CurrentAccountId long accountId, @Valid @RequestBody PlayReportRequest req) {
        return play.report(accountId, req.playId(), req.caughtCount());
    }
}
