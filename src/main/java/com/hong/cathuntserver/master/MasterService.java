package com.hong.cathuntserver.master;

import com.hong.cathuntserver.catalog.CatDef;
import com.hong.cathuntserver.catalog.CatDefRepository;
import com.hong.cathuntserver.master.dto.MasterResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 기준 데이터를 모아 GET /master 응답으로 만든다(고양이 카탈로그 + 뽑기 수치).
@Service
public class MasterService {

    private final CatDefRepository catDefs;
    private final GachaProperties gacha;

    // 카탈로그 리포지토리와 뽑기 설정값을 주입받는다.
    public MasterService(CatDefRepository catDefs, GachaProperties gacha) {
        this.catDefs = catDefs;
        this.gacha = gacha;
    }

    // 카탈로그(DB)와 뽑기 수치(설정값)를 합쳐 기준 데이터 응답을 만든다.
    @Transactional(readOnly = true)
    public MasterResponse load() {
        List<MasterResponse.CatDto> cats = catDefs.findAllByOrderBySortOrderAsc().stream()
            .map(this::toCatDto)
            .toList();

        var weights = new MasterResponse.WeightsDto(
            gacha.weights().common(), gacha.weights().rare(), gacha.weights().legend());
        var gachaDto = new MasterResponse.GachaDto(gacha.cost(), gacha.dupeRefundPercent(), weights);

        return new MasterResponse(gachaDto, cats);
    }

    // 카탈로그 한 마리를 응답 모양으로 바꾼다(등급 enum은 문자열 이름으로).
    private MasterResponse.CatDto toCatDto(CatDef c) {
        return new MasterResponse.CatDto(c.getCode(), c.getName(), c.getRarity().name(), c.getSortOrder());
    }
}
