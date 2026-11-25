package com.hong.cathuntserver.play;

import com.hong.cathuntserver.play.dto.PlayReportResponse;
import com.hong.cathuntserver.play.dto.PlayStartResponse;
import com.hong.cathuntserver.wallet.Wallet;
import com.hong.cathuntserver.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// 한 판 흐름. 시작 때 플레이 ID를 발급하고, 보고 때 그 ID로 보상을 한 번만 반영한다(서버가 계산).
@Service
public class PlayService {

    private final PlaySessionRepository plays;
    private final WalletRepository wallets;
    private final PlayProperties props;

    // 리포지토리들과 보상 설정값을 주입받는다.
    public PlayService(PlaySessionRepository plays, WalletRepository wallets, PlayProperties props) {
        this.plays = plays;
        this.wallets = wallets;
        this.props = props;
    }

    // 한 판 시작: 플레이 ID(UUID)를 만들어 STARTED로 저장하고 돌려준다.
    @Transactional
    public PlayStartResponse start(long accountId) {
        String playId = UUID.randomUUID().toString();
        plays.save(PlaySession.start(accountId, playId));
        return new PlayStartResponse(playId);
    }

    // 한 판 보고: 잡은 쥐 수로 보상을 계산해 지갑에 반영하고 레벨업까지 처리한다.
    @Transactional
    public PlayReportResponse report(long accountId, String playId, int caughtCount) {
        // 1) 내 계정의 그 플레이를 찾는다. 없으면 잘못된 보고.
        PlaySession session = plays.findByAccountIdAndPlayId(accountId, playId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "플레이를 찾을 수 없습니다"));

        // 2) 멱등: 이미 보고된 판이면 새로 반영하지 않고 저장된 결과를 그대로 돌려준다.
        if (session.getStatus() == PlayStatus.REPORTED) {
            return toResponse(session);
        }

        // 3) 비정상 값 방어: 음수는 0, 상한을 넘으면 상한으로 깎는다.
        int caught = caughtCount;
        if (caught < 0) caught = 0;
        if (caught > props.maxCatch()) caught = props.maxCatch();

        // 4) 보상 계산(서버 기준 수치). 재화는 클라가 보낸 값이 아니라 여기서 만든 값만 쓴다.
        int pawReward = caught * props.pawPerCatch();
        int expReward = caught * props.expPerCatch();

        // 5) 지갑에 반영 + 레벨업 처리.
        Wallet wallet = wallets.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다"));
        wallet.addPawJelly(pawReward);

        int beforeLevel = wallet.getLevel();
        Leveling.Progress p = Leveling.apply(wallet.getLevel(), wallet.getExp() + expReward, props.expToNextLevel());
        wallet.setLevel(p.level());
        wallet.setExp(p.exp());
        boolean leveledUp = p.level() > beforeLevel;

        // 6) 결과를 기록(멱등 재전송 대비)하고 돌려준다.
        session.markReported(caught, pawReward, expReward, p.level(), p.exp(), wallet.getPawJelly(), leveledUp);
        return toResponse(session);
    }

    // 저장된 판 결과를 응답 모양으로 바꾼다(새 보고·재전송 모두 같은 경로).
    private PlayReportResponse toResponse(PlaySession s) {
        return new PlayReportResponse(
            s.getCaughtCount(), s.getPawReward(), s.getExpReward(),
            s.isLeveledUp(), s.getResultLevel(), s.getResultExp(), s.getResultPawJelly());
    }
}
