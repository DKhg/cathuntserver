package com.hong.cathuntserver.master.dto;

import java.util.List;

// 기준 데이터(GET /master, 모든 계정 공통). 앱 시작 때 한 번 받아 캐시한다.
public record MasterResponse(
    GachaDto gacha,
    List<CatDto> cats
) {
    public record GachaDto(int cost, int dupeRefundPercent, WeightsDto weights) { }

    public record WeightsDto(int common, int rare, int legend) { }

    public record CatDto(String code, String name, String rarity, int sortOrder) { }
}
