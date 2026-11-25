package com.hong.cathuntserver.deco;

import jakarta.persistence.*;

// 어떤 고양이의 어느 자리에 어떤 장식을 착용했는지. (account_id, cat_code, slot) 유일 — 자리마다 하나.
@Entity
@Table(name = "cat_equip",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "cat_code", "slot"}))
public class CatEquip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "cat_code", nullable = false, length = 32)
    private String catCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot", nullable = false, length = 8)
    private DecoSlot slot;

    @Column(name = "decoration_code", nullable = false, length = 32)
    private String decorationCode;

    protected CatEquip() { }

    public CatEquip(Long accountId, String catCode, DecoSlot slot, String decorationCode) {
        this.accountId = accountId;
        this.catCode = catCode;
        this.slot = slot;
        this.decorationCode = decorationCode;
    }

    // 값 읽기/쓰기용 getter·setter(자리의 장식을 바꿀 때 set).
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getCatCode() { return catCode; }
    public DecoSlot getSlot() { return slot; }
    public String getDecorationCode() { return decorationCode; }
    public void setDecorationCode(String decorationCode) { this.decorationCode = decorationCode; }
}
