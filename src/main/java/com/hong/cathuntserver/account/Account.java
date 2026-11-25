package com.hong.cathuntserver.account;

import jakarta.persistence.*;
import java.time.Instant;

// DB account 테이블 한 행 = 한 계정. 기기 키(device_key)로 계정을 찾는다.
@Entity
@Table(name = "account",
       uniqueConstraints = @UniqueConstraint(columnNames = "device_key"))
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_key", nullable = false, unique = true, length = 64)
    private String deviceKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt;

    @Column(name = "nickname", length = 32)
    private String nickname;              // 집사(플레이어) 닉네임. null이면 미설정

    @Column(name = "representative_cat", nullable = false, length = 32)
    private String representativeCat;     // 대표 고양이 code(스테이지에 데리고 나가는 1마리)

    protected Account() { }  // JPA용 기본 생성자

    // 처음 보는 기기 키로 새 계정을 만든다.
    public static Account createNew(String deviceKey, String starterCat) {
        Account a = new Account();
        Instant now = Instant.now();
        a.deviceKey = deviceKey;
        a.createdAt = now;
        a.lastLoginAt = now;
        a.representativeCat = starterCat;
        return a;
    }

    // 로그인할 때 마지막 로그인 시각을 갱신한다.
    public void touchLogin() { this.lastLoginAt = Instant.now(); }

    // 아래는 필드 값을 읽고/바꾸는 getter·setter. 외부(서비스)에서 이 값들을 읽어 응답을 만든다.
    public Long getId() { return id; }
    public String getDeviceKey() { return deviceKey; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRepresentativeCat() { return representativeCat; }
    public void setRepresentativeCat(String representativeCat) { this.representativeCat = representativeCat; }
}
