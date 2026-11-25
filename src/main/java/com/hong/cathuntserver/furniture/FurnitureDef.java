package com.hong.cathuntserver.furniture;

import jakarta.persistence.*;

// 가구(방 꾸미기) 카탈로그(마스터, 공통). 가격은 서버 기준 데이터. GET /master로 내려준다.
@Entity
@Table(name = "furniture_def")
public class FurnitureDef {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Column(name = "price", nullable = false)
    private long price;

    protected FurnitureDef() { }

    public FurnitureDef(String code, String name, long price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    // 값 읽기용 getter.
    public String getCode() { return code; }
    public String getName() { return name; }
    public long getPrice() { return price; }
}
