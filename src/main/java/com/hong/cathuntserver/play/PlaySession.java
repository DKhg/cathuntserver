package com.hong.cathuntserver.play;

import jakarta.persistence.*;
import java.time.Instant;

// 한 판 기록. 시작 때 만들어 playId를 돌려주고, 보고 때 이 행을 찾아 보상을 한 번만 반영한다(멱등).
// 같은 playId로 또 보고가 와도 이미 REPORTED면 저장된 결과만 돌려준다(보상 중복 지급 방지, PRD 9.4).
@Entity
@Table(name = "play_session",
       uniqueConstraints = @UniqueConstraint(columnNames = "play_id"))
public class PlaySession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "play_id", nullable = false, length = 64)
    private String playId;        // 시작 때 서버가 발급하는 플레이 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PlayStatus status;

    // --- 보고 때 채워지는 결과(멱등 재전송에 그대로 돌려준다) ---
    @Column(name = "caught_count")
    private int caughtCount;
    @Column(name = "paw_reward")
    private int pawReward;
    @Column(name = "exp_reward")
    private int expReward;
    @Column(name = "result_level")
    private int resultLevel;
    @Column(name = "result_exp")
    private long resultExp;
    @Column(name = "result_paw_jelly")
    private long resultPawJelly;
    @Column(name = "leveled_up")
    private boolean leveledUp;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;
    @Column(name = "reported_at")
    private Instant reportedAt;

    protected PlaySession() { }

    // 한 판 시작: 플레이 ID를 달고 STARTED 상태로 만든다.
    public static PlaySession start(Long accountId, String playId) {
        PlaySession s = new PlaySession();
        s.accountId = accountId;
        s.playId = playId;
        s.status = PlayStatus.STARTED;
        s.startedAt = Instant.now();
        return s;
    }

    // 결과를 기록하고 상태를 REPORTED로 바꾼다.
    public void markReported(int caughtCount, int pawReward, int expReward,
                             int resultLevel, long resultExp, long resultPawJelly, boolean leveledUp) {
        this.caughtCount = caughtCount;
        this.pawReward = pawReward;
        this.expReward = expReward;
        this.resultLevel = resultLevel;
        this.resultExp = resultExp;
        this.resultPawJelly = resultPawJelly;
        this.leveledUp = leveledUp;
        this.status = PlayStatus.REPORTED;
        this.reportedAt = Instant.now();
    }

    // 값 읽기용 getter.
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getPlayId() { return playId; }
    public PlayStatus getStatus() { return status; }
    public int getCaughtCount() { return caughtCount; }
    public int getPawReward() { return pawReward; }
    public int getExpReward() { return expReward; }
    public int getResultLevel() { return resultLevel; }
    public long getResultExp() { return resultExp; }
    public long getResultPawJelly() { return resultPawJelly; }
    public boolean isLeveledUp() { return leveledUp; }
}
