package com.hong.cathuntserver.gacha;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GachaRequestRepository extends JpaRepository<GachaRequest, Long> {
    // 같은 계정이 같은 요청 키로 이미 뽑았는지 찾는다(멱등 처리용).
    Optional<GachaRequest> findByAccountIdAndRequestKey(Long accountId, String requestKey);
}
