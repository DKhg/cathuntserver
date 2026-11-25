package com.hong.cathuntserver.gacha.dto;

// 뽑기 결과. 클라는 이 값만 화면에 반영한다(재화는 서버가 계산한 pawJellyLeft를 그대로 쓴다).
public record GachaDrawResponse(
    String catCode,
    String catName,
    String rarity,
    boolean isNew,       // 새 고양이면 true, 중복이면 false
    int dupeRefund,      // 중복이라 돌려받은 발바닥(새 고양이면 0)
    long pawJellyLeft    // 뽑고 난 뒤 남은 발바닥
) { }
