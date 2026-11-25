package com.hong.cathuntserver.play.dto;

// 한 판 시작 응답: 서버가 발급한 플레이 ID. 클라는 이걸 들고 있다가 결과 보고 때 보낸다.
public record PlayStartResponse(String playId) { }
