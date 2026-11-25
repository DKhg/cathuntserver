package com.hong.cathuntserver.gacha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 뽑기 요청 본문. requestKey는 클라가 만든 멱등 키(한 번의 뽑기당 하나). 재전송 때 같은 키를 쓴다.
public record GachaDrawRequest(
    @NotBlank @Size(max = 64) String requestKey
) { }
