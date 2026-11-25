package com.hong.cathuntserver.deco;

import jakarta.persistence.*;

// 장식(고양이 꾸미기) 카탈로그(마스터, 공통). 가격은 서버 기준 데이터. GET /master로 내려준다.
@Entity
@Table(name = "decoration_def")
public class DecorationDef {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot", nullable = false, length = 8)
    private DecoSlot slot;      // 머리/얼굴/목

    @Column(name = "price", nullable = false)
    private long price;         // 분홍 젤리 가격

    protected DecorationDef() { }

    public DecorationDef(String code, String name, DecoSlot slot, long price) {
        this.code = code;
        this.name = name;
        this.slot = slot;
        this.price = price;
    }

    // 값 읽기용 getter.
    public String getCode() { return code; }
    public String getName() { return name; }
    public DecoSlot getSlot() { return slot; }
    public long getPrice() { return price; }
}
