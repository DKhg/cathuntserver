package com.hong.cathuntserver.common;

// 모든 에러 응답의 공통 모양. 클라이언트는 code로 분기하고 message를 사용자에게 보여줄 수 있다.
// 예: {"code":"BAD_REQUEST","message":"발바닥이 부족합니다"}
public record ApiError(String code, String message) { }
