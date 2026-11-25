package com.hong.cathuntserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 게스트 로그인 요청 본문: 기기 키 하나.
public record GuestLoginRequest(
    @NotBlank @Size(max = 64) String deviceKey
) { }
