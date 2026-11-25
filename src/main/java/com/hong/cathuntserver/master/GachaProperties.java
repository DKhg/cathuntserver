package com.hong.cathuntserver.master;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 뽑기 기준 데이터(application.yaml의 app.gacha.*). 서버가 원본으로 들고 있는다.
@ConfigurationProperties(prefix = "app.gacha")
public record GachaProperties(
    int cost,
    int dupeRefundPercent,
    Weights weights
) {
    // 등급별 확률 가중치(흔한/귀한/전설). 합이 100일 필요는 없고 비율로 쓴다.
    public record Weights(int common, int rare, int legend) { }
}
