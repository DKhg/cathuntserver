package com.hong.cathuntserver.deco;

import jakarta.persistence.*;

// 계정이 보유한 장식. (account_id, decoration_code) 유일.
@Entity
@Table(name = "owned_decoration",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "decoration_code"}))
public class OwnedDecoration {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "decoration_code", nullable = false, length = 32)
    private String decorationCode;

    protected OwnedDecoration() { }

    public OwnedDecoration(Long accountId, String decorationCode) {
        this.accountId = accountId;
        this.decorationCode = decorationCode;
    }

    // 값 읽기용 getter.
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getDecorationCode() { return decorationCode; }
}
