package com.hong.cathuntserver.gacha;

import jakarta.persistence.*;
import java.time.Instant;

// 한 번의 뽑기 요청 기록. 같은 (account_id, request_key)가 또 오면 새로 뽑지 않고 이 결과를 그대로 돌려준다(멱등).
// 네트워크가 끊겨 클라가 같은 요청을 재전송해도 발바닥이 두 번 깎이지 않게 하는 안전장치(PRD 9.4).
@Entity
@Table(name = "gacha_request",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "request_key"}))
public class GachaRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "request_key", nullable = false, length = 64)
    private String requestKey;    // 클라가 만든 요청 키(멱등 키)

    @Column(name = "cat_code", nullable = false, length = 32)
    private String catCode;       // 이번에 뽑힌 고양이

    @Column(name = "is_new", nullable = false)
    private boolean isNew;        // 새 고양이면 true, 중복이면 false

    @Column(name = "dupe_refund", nullable = false)
    private int dupeRefund;       // 중복이라 돌려준 발바닥(새 고양이면 0)

    @Column(name = "paw_jelly_left", nullable = false)
    private long pawJellyLeft;    // 뽑고 난 뒤 남은 발바닥

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected GachaRequest() { }

    public GachaRequest(Long accountId, String requestKey, String catCode,
                        boolean isNew, int dupeRefund, long pawJellyLeft) {
        this.accountId = accountId;
        this.requestKey = requestKey;
        this.catCode = catCode;
        this.isNew = isNew;
        this.dupeRefund = dupeRefund;
        this.pawJellyLeft = pawJellyLeft;
        this.createdAt = Instant.now();
    }

    // 값 읽기용 getter.
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getRequestKey() { return requestKey; }
    public String getCatCode() { return catCode; }
    public boolean isNew() { return isNew; }
    public int getDupeRefund() { return dupeRefund; }
    public long getPawJellyLeft() { return pawJellyLeft; }
}
