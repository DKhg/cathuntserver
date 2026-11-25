package com.hong.cathuntserver.account;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 계정 삭제(출시 심사 필수: 사용자가 자기 데이터를 지울 수 있어야 함).
// 계정에 딸린 모든 데이터를 먼저 지우고, 마지막에 계정 자체를 지운다. 한 트랜잭션이라 전부 지워지거나 전부 취소된다.
@Service
public class AccountDeletionService {

    private final EntityManager em;

    public AccountDeletionService(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void delete(long accountId) {
        // 자식 데이터부터(FK 제약은 없지만 순서를 지켜 깔끔히). 계정별 테이블 전부.
        deleteBy("gacha_request", accountId);
        deleteBy("play_session", accountId);
        deleteBy("owned_cat", accountId);
        deleteBy("owned_decoration", accountId);
        deleteBy("cat_equip", accountId);
        deleteBy("owned_furniture", accountId);
        deleteBy("room_placement", accountId);
        deleteBy("wallet", accountId);
        // 마지막으로 계정
        em.createNativeQuery("DELETE FROM account WHERE id = ?1")
            .setParameter(1, accountId)
            .executeUpdate();
    }

    private void deleteBy(String table, long accountId) {
        em.createNativeQuery("DELETE FROM " + table + " WHERE account_id = ?1")
            .setParameter(1, accountId)
            .executeUpdate();
    }
}
