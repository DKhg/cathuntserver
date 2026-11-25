package com.hong.cathuntserver.account;

import com.hong.cathuntserver.cat.OwnedCat;
import com.hong.cathuntserver.cat.OwnedCatRepository;
import com.hong.cathuntserver.wallet.Wallet;
import com.hong.cathuntserver.wallet.WalletRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 게스트 로그인 흐름: 기기 키가 처음이면 계정+지갑+시작 고양이를 만들고, 있으면 로그인만 한다.
@Service
public class GuestLoginService {

    private final AccountRepository accounts;
    private final WalletRepository wallets;
    private final OwnedCatRepository ownedCats;
    // 트랜잭션 경계를 얻기 위한 '자기(프록시) 참조'. 같은 빈의 @Transactional 메서드를 그냥 부르면
    // 프록시를 거치지 않아 트랜잭션이 안 열린다. ObjectProvider로 프록시를 받아 호출한다(순환 주입도 피함).
    private final ObjectProvider<GuestLoginService> self;

    private final long startPawJelly;
    private final int startLevel;
    private final String starterCat;

    // 리포지토리 3개와, 새 계정 시작값(@Value로 application.yaml의 app.new-account.*를 읽어 넣음)을 주입받는다.
    public GuestLoginService(AccountRepository accounts,
                             WalletRepository wallets,
                             OwnedCatRepository ownedCats,
                             ObjectProvider<GuestLoginService> self,
                             @Value("${app.new-account.paw-jelly}") long startPawJelly,
                             @Value("${app.new-account.start-level}") int startLevel,
                             @Value("${app.new-account.starter-cat}") String starterCat) {
        this.accounts = accounts;
        this.wallets = wallets;
        this.ownedCats = ownedCats;
        this.self = self;
        this.startPawJelly = startPawJelly;
        this.startLevel = startLevel;
        this.starterCat = starterCat;
    }

    // 로그인 결과: 계정과, 이번에 새로 만든 계정인지.
    public record Result(Account account, boolean isNew) { }

    // 게스트 로그인 바깥 경계(트랜잭션 아님). 실제 DB 쓰기는 아래 @Transactional 메서드를 프록시로 부른다.
    // 같은 기기 키로 '동시에' 첫 로그인이 두 번 들어오면, 둘 다 '없음'을 보고 새 계정을 만들려다
    // 하나가 device_key unique 제약에 걸려 DataIntegrityViolationException이 난다. 그걸 잡아
    // 먼저 만들어진 계정으로 로그인시킨다(재로그인). 트랜잭션은 provisionNew 안에서만 열리므로,
    // 실패한 쪽 트랜잭션은 깔끔히 롤백되고 여기서 다시 조회할 수 있다.
    public Result login(String deviceKey) {
        var existing = accounts.findByDeviceKey(deviceKey);
        if (existing.isPresent()) {
            return self.getObject().touchExisting(existing.get().getId());
        }
        try {
            return self.getObject().provisionNew(deviceKey);
        } catch (DataIntegrityViolationException race) {
            // 동시에 다른 요청이 먼저 계정을 만든 경우 → 그 계정으로 로그인.
            var acc = accounts.findByDeviceKey(deviceKey).orElseThrow(() -> race);
            return self.getObject().touchExisting(acc.getId());
        }
    }

    // 새 계정 + 지갑 + 시작 고양이를 한 트랜잭션에서 만든다(전부 성공하거나 전부 취소).
    // device_key가 겹치면 save 시점에 DataIntegrityViolationException이 나고 이 트랜잭션은 롤백된다.
    @Transactional
    public Result provisionNew(String deviceKey) {
        var acc = accounts.save(Account.createNew(deviceKey, starterCat));
        wallets.save(new Wallet(acc.getId(), startPawJelly, 0, startLevel));
        ownedCats.save(new OwnedCat(acc.getId(), starterCat));
        return new Result(acc, true);
    }

    // 기존 계정 로그인: 마지막 로그인 시각만 갱신한다(dirty checking으로 커밋 때 저장).
    @Transactional
    public Result touchExisting(long accountId) {
        var acc = accounts.findById(accountId).orElseThrow();
        acc.touchLogin();
        return new Result(acc, false);
    }
}
