package com.hong.cathuntserver.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatDefRepository extends JpaRepository<CatDef, String> {
    // 도감 순서(sort_order 오름차순)로 전체 카탈로그를 가져온다. GET /master에서 쓴다.
    List<CatDef> findAllByOrderBySortOrderAsc();

    // 특정 등급의 고양이들만 가져온다. 뽑기에서 등급을 정한 뒤 그 안에서 한 마리 고를 때 쓴다.
    List<CatDef> findByRarity(Rarity rarity);
}
