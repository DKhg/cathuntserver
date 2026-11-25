package com.hong.cathuntserver.me.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 프로필 수정 요청들(닉네임·대표 고양이·고양이 별명). 작은 DTO라 한 파일에 모았다.
public final class ProfileRequests {

    private ProfileRequests() { }

    // 플레이어 닉네임 변경.
    public record Nickname(@NotBlank @Size(max = 32) String nickname) { }

    // 대표 고양이 변경(보유한 고양이여야 함).
    public record Representative(@NotBlank @Size(max = 32) String catCode) { }

    // 특정 고양이의 별명 변경.
    public record CatName(
        @NotBlank @Size(max = 32) String catCode,
        @NotBlank @Size(max = 32) String name
    ) { }
}
