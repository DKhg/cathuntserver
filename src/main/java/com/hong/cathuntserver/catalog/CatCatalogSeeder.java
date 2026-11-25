package com.hong.cathuntserver.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// 서버 시작 때 고양이 카탈로그(마스터)를 DB에 채운다. 이미 있으면 건너뛴다.
// id·등급은 Unity GachaConfig.asset과 동일하게 맞췄다(어긋나면 클라 동기화가 깨짐).
@Component
public class CatCatalogSeeder implements CommandLineRunner {

    private final CatDefRepository cats;

    public CatCatalogSeeder(CatDefRepository cats) {
        this.cats = cats;
    }

    // CommandLineRunner라서 서버가 다 뜬 직후 이 run()이 한 번 자동 실행된다.
    // MVP 7마리를 넣되, 이미 있는 code는 건너뛰어 재시작 때 중복/덮어쓰기를 막는다.
    // ⚠️ 여기 목록이 Unity GachaConfig.asset과 "정확히 같은 7종"이어야 한다. 예전엔 12마리였는데,
    //    목록이 많으면 서버 재시작 때마다 빠진 고양이를 도로 넣어 뽑기에 7종 밖 고양이가 나온다.
    @Override
    public void run(String... args) {
        List<CatDef> catalog = List.of(
            // 흔한 냥 4
            new CatDef("cheese",       "치즈냥", Rarity.COMMON, 1),
            new CatDef("calico",       "삼색냥", Rarity.COMMON, 2),
            new CatDef("white",        "흰둥냥", Rarity.COMMON, 3),
            new CatDef("graytabby",    "회색냥", Rarity.COMMON, 4),
            // 귀한 냥 2
            new CatDef("scottishfold", "스코냥", Rarity.RARE,   5),
            new CatDef("bengal",       "벵갈냥", Rarity.RARE,   6),
            // 전설의 냥 1
            new CatDef("siamese",      "샴냥",   Rarity.LEGEND, 7)
        );
        for (CatDef c : catalog) {
            if (!cats.existsById(c.getCode())) {
                cats.save(c);
            }
        }
    }
}
