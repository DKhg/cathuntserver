package com.hong.cathuntserver.wallet;

import jakarta.persistence.*;

// 계정별 재화. account 한 행당 wallet 한 행(id = accountId 공유).
// 서버가 재화를 계산한다 — 클라가 보낸 값은 믿지 않는다(PRD 9.4).
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "paw_jelly", nullable = false)
    private long pawJelly;   // 분홍 젤리(발바닥) = 돈. 뽑기·가구 구매에 쓴다

    @Column(name = "exp", nullable = false)
    private long exp;        // 민트 젤리 = 경험치

    @Column(name = "level", nullable = false)
    private int level;

    protected Wallet() { }

    public Wallet(Long accountId, long pawJelly, long exp, int level) {
        this.accountId = accountId;
        this.pawJelly = pawJelly;
        this.exp = exp;
        this.level = level;
    }

    // 값 읽기용 getter.
    public Long getAccountId() { return accountId; }
    public long getPawJelly() { return pawJelly; }
    public long getExp() { return exp; }
    public int getLevel() { return level; }

    // 재화 증감은 서버에서만. (뽑기·보상 계산이 들어올 때 여기로 모은다)
    public void addPawJelly(long delta) { this.pawJelly += delta; }
    public void addExp(long delta) { this.exp += delta; }
    // 레벨업 계산 결과(남은 경험치)를 그대로 넣을 때 쓴다.
    public void setExp(long exp) { this.exp = exp; }
    public void setLevel(int level) { this.level = level; }
}
