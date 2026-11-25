package com.hong.cathuntserver.furniture;

import jakarta.persistence.*;

// 계정이 보유한 가구. (account_id, furniture_code) 유일.
@Entity
@Table(name = "owned_furniture",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "furniture_code"}))
public class OwnedFurniture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "furniture_code", nullable = false, length = 32)
    private String furnitureCode;

    protected OwnedFurniture() { }

    public OwnedFurniture(Long accountId, String furnitureCode) {
        this.accountId = accountId;
        this.furnitureCode = furnitureCode;
    }

    // 값 읽기용 getter.
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getFurnitureCode() { return furnitureCode; }
}
