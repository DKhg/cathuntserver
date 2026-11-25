package com.hong.cathuntserver.play.dto;

// 한 판 보고 결과. 클라는 이 값만 화면에 반영한다(보상·레벨은 서버가 계산한 값 그대로).
public record PlayReportResponse(
    int caughtCount,     // 서버가 인정한 포획 수(비정상 값은 깎일 수 있음)
    int pawReward,       // 이번에 받은 발바닥
    int expReward,       // 이번에 받은 경험치
    boolean leveledUp,   // 이번에 레벨이 올랐는지
    int level,           // 반영 후 레벨
    long exp,            // 반영 후 현재 레벨 진행 경험치
    long pawJelly        // 반영 후 총 발바닥
) { }
