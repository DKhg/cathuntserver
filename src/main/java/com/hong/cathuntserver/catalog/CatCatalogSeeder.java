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
    // 카탈로그 12마리를 넣되, 이미 있는 code는 건너뛰어 재시작 때 중복/덮어쓰기를 막는다.
    @Override
    public void run(String... args) {
        List<CatDef> catalog = List.of(
            new CatDef("cheese",            "치즈냥",   Rarity.COMMON, 1),
            new CatDef("calico",            "삼색냥",   Rarity.COMMON, 2),
            new CatDef("black",             "까망냥",   Rarity.COMMON, 3),
            new CatDef("white",             "흰둥냥",   Rarity.COMMON, 4),
            new CatDef("graytabby",         "회색냥",   Rarity.COMMON, 5),
            new CatDef("tuxedo",            "턱시도냥", Rarity.COMMON, 6),
            new CatDef("siamese",           "샴냥",     Rarity.RARE,   7),
            new CatDef("scottishfold",      "스코냥",   Rarity.RARE,   8),
            new CatDef("americanshorthair", "아메숏냥", Rarity.RARE,   9),
            new CatDef("persian",           "페르시안냥", Rarity.RARE, 10),
            new CatDef("bengal",            "벵갈냥",   Rarity.LEGEND, 11),
            new CatDef("ragdoll",           "래그돌냥", Rarity.LEGEND, 12)
        );
        for (CatDef c : catalog) {
            if (!cats.existsById(c.getCode())) {
                cats.save(c);
            }
        }
    }
}
