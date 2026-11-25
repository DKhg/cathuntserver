package com.hong.cathuntserver.me;

import com.hong.cathuntserver.account.Account;
import com.hong.cathuntserver.account.AccountRepository;
import com.hong.cathuntserver.catalog.CatDef;
import com.hong.cathuntserver.catalog.CatDefRepository;
import com.hong.cathuntserver.cat.OwnedCat;
import com.hong.cathuntserver.cat.OwnedCatRepository;
import com.hong.cathuntserver.me.dto.MeResponse;
import com.hong.cathuntserver.wallet.Wallet;
import com.hong.cathuntserver.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// 계정의 전체 상태(계정·지갑·보유 고양이)를 모아 GET /me 응답으로 만든다.
@Service
public class MeService {

    private final AccountRepository accounts;
    private final WalletRepository wallets;
    private final OwnedCatRepository ownedCats;
    private final CatDefRepository catDefs;

    // 필요한 리포지토리 4개를 주입받는다.
    public MeService(AccountRepository accounts, WalletRepository wallets,
                     OwnedCatRepository ownedCats, CatDefRepository catDefs) {
        this.accounts = accounts;
        this.wallets = wallets;
        this.ownedCats = ownedCats;
        this.catDefs = catDefs;
    }

    // 계정 ID로 account·wallet·owned_cat을 DB에서 읽어 하나의 응답으로 합친다.
    // readOnly=true: 읽기만 하는 트랜잭션이라고 알려 주면 DB가 조금 더 효율적으로 처리한다.
    @Transactional(readOnly = true)
    public MeResponse load(long accountId) {
        Account account = accounts.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다"));
        Wallet wallet = wallets.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다"));

        // 카탈로그를 code로 한 번에 조회해 보유 고양이에 이름·등급을 붙인다.
        Map<String, CatDef> catalog = catDefs.findAll().stream()
            .collect(Collectors.toMap(CatDef::getCode, Function.identity()));

        List<MeResponse.OwnedCatDto> cats = ownedCats.findByAccountId(accountId).stream()
            .map(oc -> toCatDto(oc, catalog.get(oc.getCatCode())))
            .toList();

        var walletDto = new MeResponse.WalletDto(wallet.getPawJelly(), wallet.getExp(), wallet.getLevel());
        return new MeResponse(account.getId(), account.getNickname(),
            account.getRepresentativeCat(), walletDto, cats);
    }

    // 보유 고양이 한 마리를 응답 모양으로 바꾼다. 카탈로그(def)가 있으면 이름·등급을 붙이고,
    // 혹시 없으면(데이터 불일치) code를 이름 대신 쓴다.
    private MeResponse.OwnedCatDto toCatDto(OwnedCat oc, CatDef def) {
        String name = def != null ? def.getName() : oc.getCatCode();
        String rarity = def != null ? def.getRarity().name() : null;
        return new MeResponse.OwnedCatDto(oc.getCatCode(), name, rarity, oc.getCustomName());
    }
}
