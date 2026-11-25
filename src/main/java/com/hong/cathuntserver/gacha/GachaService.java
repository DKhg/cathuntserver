package com.hong.cathuntserver.gacha;

import com.hong.cathuntserver.cat.OwnedCat;
import com.hong.cathuntserver.cat.OwnedCatRepository;
import com.hong.cathuntserver.catalog.CatDef;
import com.hong.cathuntserver.catalog.CatDefRepository;
import com.hong.cathuntserver.catalog.Rarity;
import com.hong.cathuntserver.gacha.dto.GachaDrawResponse;
import com.hong.cathuntserver.master.GachaProperties;
import com.hong.cathuntserver.wallet.Wallet;
import com.hong.cathuntserver.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// 뽑기 본체. 비용 차감·확률·중복환급을 전부 서버가 계산한다(클라가 보낸 재화는 믿지 않는다 — PRD 9.4).
@Service
public class GachaService {

    private final WalletRepository wallets;
    private final OwnedCatRepository ownedCats;
    private final CatDefRepository catDefs;
    private final GachaRequestRepository requests;
    private final GachaProperties gacha;

    // 필요한 리포지토리들과 뽑기 설정값을 주입받는다.
    public GachaService(WalletRepository wallets, OwnedCatRepository ownedCats,
                        CatDefRepository catDefs, GachaRequestRepository requests,
                        GachaProperties gacha) {
        this.wallets = wallets;
        this.ownedCats = ownedCats;
        this.catDefs = catDefs;
        this.requests = requests;
        this.gacha = gacha;
    }

    // 뽑기 1회. @Transactional이라 차감·지급·기록이 전부 성공하거나 전부 취소된다.
    @Transactional
    public GachaDrawResponse draw(long accountId, String requestKey) {
        // 1) 멱등: 같은 요청 키로 이미 뽑았다면 새로 뽑지 않고 그때 결과를 그대로 돌려준다(중복 차감 방지).
        var already = requests.findByAccountIdAndRequestKey(accountId, requestKey);
        if (already.isPresent()) {
            return toResponse(already.get());
        }

        // 2) 지갑을 가져와 비용을 낼 수 있는지 본다. 모자라면 아무것도 바꾸지 않고 400을 낸다(요청 키도 소모 안 함).
        Wallet wallet = wallets.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다"));
        int cost = gacha.cost();
        if (wallet.getPawJelly() < cost) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "발바닥이 부족합니다");
        }

        // 3) 비용 차감 → 등급 뽑기 → 그 등급에서 고양이 한 마리.
        wallet.addPawJelly(-cost);
        Rarity rarity = rollRarity();
        CatDef cat = pickOne(rarity);

        // 4) 이미 가진 고양이면 중복 → 비용의 일부를 환급. 아니면 도감에 추가.
        boolean owned = ownedCats.existsByAccountIdAndCatCode(accountId, cat.getCode());
        int dupeRefund = 0;
        boolean isNew;
        if (owned) {
            dupeRefund = refund(cost, gacha.dupeRefundPercent());
            wallet.addPawJelly(dupeRefund);
            isNew = false;
        } else {
            ownedCats.save(new OwnedCat(accountId, cat.getCode()));
            isNew = true;
        }

        // 5) 결과를 멱등 기록으로 남기고 돌려준다.
        var saved = requests.save(new GachaRequest(
            accountId, requestKey, cat.getCode(), isNew, dupeRefund, wallet.getPawJelly()));
        return toResponse(saved);
    }

    // 등급 뽑기: 가중치(흔한70·귀한25·전설5) 비율로 0~합 사이 난수를 굴려 구간으로 등급을 정한다.
    private Rarity rollRarity() {
        int common = gacha.weights().common();
        int rare = gacha.weights().rare();
        int legend = gacha.weights().legend();
        int total = common + rare + legend;
        int roll = ThreadLocalRandom.current().nextInt(total);  // 0 이상 total 미만
        if (roll < common) return Rarity.COMMON;
        if (roll < common + rare) return Rarity.RARE;
        return Rarity.LEGEND;
    }

    // 정한 등급 안에서 고양이 한 마리를 균등 확률로 고른다.
    private CatDef pickOne(Rarity rarity) {
        List<CatDef> pool = catDefs.findByRarity(rarity);
        if (pool.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "뽑을 고양이가 없습니다: " + rarity);
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    // 중복 환급액: 비용 × 퍼센트, 반올림. 음수는 0으로 막는다. (클라 Gacha.Refund와 같은 규칙)
    private int refund(int cost, int percent) {
        if (cost <= 0 || percent <= 0) return 0;
        return (int) Math.round(cost * percent / 100.0);
    }

    // 뽑기 기록을 응답 모양으로 바꾼다. 고양이 이름·등급은 카탈로그에서 가져온다(새 뽑기·재전송 모두 같은 경로).
    private GachaDrawResponse toResponse(GachaRequest r) {
        CatDef def = catDefs.findById(r.getCatCode()).orElse(null);
        String name = def != null ? def.getName() : r.getCatCode();
        String rarity = def != null ? def.getRarity().name() : null;
        return new GachaDrawResponse(r.getCatCode(), name, rarity, r.isNew(), r.getDupeRefund(), r.getPawJellyLeft());
    }
}
