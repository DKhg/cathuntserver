package com.hong.cathuntserver.cat;

import jakarta.persistence.*;

// 계정이 보유한 고양이 한 마리. (account_id, cat_code)는 유일(같은 고양이 중복 행 금지).
// 커스텀 이름(별명)도 여기에 둔다 — 고양이마다 하나.
@Entity
@Table(name = "owned_cat",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "cat_code"}))
public class OwnedCat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "cat_code", nullable = false, length = 32)
    private String catCode;   // CatDef.code 참조(마스터)

    @Column(name = "custom_name", length = 32)
    private String customName; // null이면 카탈로그 기본 이름 사용

    protected OwnedCat() { }

    public OwnedCat(Long accountId, String catCode) {
        this.accountId = accountId;
        this.catCode = catCode;
    }

    // 값 읽기/쓰기용 getter·setter(별명은 나중에 이름 바꾸기에서 set).
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getCatCode() { return catCode; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
}
