package com.hong.cathuntserver.auth.dto;

// 게스트 로그인 응답: 계정 ID, 로그인 토큰, 이번에 새로 만든 계정인지.
public record GuestLoginResponse(
    long accountId,
    String token,
    boolean isNew
) { }
