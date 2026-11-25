package com.hong.cathuntserver.me;

import com.hong.cathuntserver.account.Account;
import com.hong.cathuntserver.account.AccountRepository;
import com.hong.cathuntserver.auth.CurrentAccountId;
import com.hong.cathuntserver.cat.OwnedCat;
import com.hong.cathuntserver.cat.OwnedCatRepository;
import com.hong.cathuntserver.me.dto.MeResponse;
import com.hong.cathuntserver.me.dto.ProfileRequests;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// 프로필 수정(닉네임·대표 고양이·고양이 별명). 모두 토큰 필요. 수정 후 최신 /me를 돌려줘 클라가 바로 갱신하게 한다.
@RestController
@RequestMapping("/me")
public class ProfileController {

    private final AccountRepository accounts;
    private final OwnedCatRepository ownedCats;
    private final MeService me;

    public ProfileController(AccountRepository accounts, OwnedCatRepository ownedCats, MeService me) {
        this.accounts = accounts;
        this.ownedCats = ownedCats;
        this.me = me;
    }

    // POST /me/nickname — 닉네임 변경.
    @PostMapping("/nickname")
    @Transactional
    public MeResponse setNickname(@CurrentAccountId long accountId, @Valid @RequestBody ProfileRequests.Nickname req) {
        Account acc = account(accountId);
        acc.setNickname(req.nickname());
        return me.load(accountId);
    }

    // POST /me/representative — 대표 고양이 변경(보유한 것만).
    @PostMapping("/representative")
    @Transactional
    public MeResponse setRepresentative(@CurrentAccountId long accountId, @Valid @RequestBody ProfileRequests.Representative req) {
        if (!ownedCats.existsByAccountIdAndCatCode(accountId, req.catCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보유하지 않은 고양이입니다");
        }
        Account acc = account(accountId);
        acc.setRepresentativeCat(req.catCode());
        return me.load(accountId);
    }

    // POST /me/catname — 특정 고양이 별명 변경.
    @PostMapping("/catname")
    @Transactional
    public MeResponse setCatName(@CurrentAccountId long accountId, @Valid @RequestBody ProfileRequests.CatName req) {
        OwnedCat cat = ownedCats.findByAccountIdAndCatCode(accountId, req.catCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "보유하지 않은 고양이입니다"));
        cat.setCustomName(req.name());
        return me.load(accountId);
    }

    private Account account(long accountId) {
        return accounts.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다"));
    }
}
