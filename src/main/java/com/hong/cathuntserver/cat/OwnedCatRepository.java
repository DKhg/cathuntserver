package com.hong.cathuntserver.cat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnedCatRepository extends JpaRepository<OwnedCat, Long> {
    List<OwnedCat> findByAccountId(Long accountId);
    boolean existsByAccountIdAndCatCode(Long accountId, String catCode);
    // 그 계정의 특정 고양이 한 마리(별명 바꿀 때 쓴다).
    java.util.Optional<OwnedCat> findByAccountIdAndCatCode(Long accountId, String catCode);
}
