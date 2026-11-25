package com.hong.cathuntserver.catalog;

import jakarta.persistence.*;

// 전체 고양이 카탈로그(마스터 데이터, 모든 계정 공통). GET /master로 내려준다.
// code는 Unity GachaConfig.asset의 id와 반드시 같아야 한다(예: "cheese").
@Entity
@Table(name = "cat_def")
public class CatDef {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 32)
    private String name;          // 화면 이름(예: 치즈냥)

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 16)
    private Rarity rarity;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;        // 도감 정렬 순서

    protected CatDef() { }

    public CatDef(String code, String name, Rarity rarity, int sortOrder) {
        this.code = code;
        this.name = name;
        this.rarity = rarity;
        this.sortOrder = sortOrder;
    }

    // 값 읽기용 getter(서비스가 응답 만들 때 쓴다).
    public String getCode() { return code; }
    public String getName() { return name; }
    public Rarity getRarity() { return rarity; }
    public int getSortOrder() { return sortOrder; }
}
