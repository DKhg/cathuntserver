package com.hong.cathuntserver.play;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// 한 판 보상 기준 데이터(application.yaml의 app.play.*). 서버가 보상을 계산할 때 쓴다.
@ConfigurationProperties(prefix = "app.play")
public record PlayProperties(
    int pawPerCatch,              // 쥐 1마리당 발바닥
    int expPerCatch,              // 쥐 1마리당 경험치
    int maxCatch,                 // 한 판 인정 최대 포획 수(비정상 값 방어)
    List<Integer> expToNextLevel  // 레벨별 다음 레벨 필요 경험치 표
) { }
