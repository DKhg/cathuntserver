package com.hong.cathuntserver.me.dto;

import java.util.List;

// 내 데이터 전체(GET /me). 클라이언트의 로비·도감·HUD가 이걸로 그린다.
public record MeResponse(
    long accountId,
    String nickname,
    String representativeCat,
    WalletDto wallet,
    List<OwnedCatDto> cats
) {
    public record WalletDto(long pawJelly, long exp, int level) { }

    // 보유 고양이: 카탈로그 이름·등급 + 내가 지은 별명(없으면 null).
    public record OwnedCatDto(String code, String name, String rarity, String customName) { }
}
