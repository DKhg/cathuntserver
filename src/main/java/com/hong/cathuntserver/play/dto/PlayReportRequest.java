package com.hong.cathuntserver.play.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 한 판 결과 보고: 시작 때 받은 playId와, 잡은 쥐 수. 보상은 서버가 계산하므로 재화 값은 보내지 않는다.
public record PlayReportRequest(
    @NotBlank @Size(max = 64) String playId,
    @Min(0) int caughtCount
) { }
