package com.hong.cathuntserver.play;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {
    // 보고 때, 내 계정의 그 플레이 ID 기록을 찾는다(없으면 잘못된 보고).
    Optional<PlaySession> findByAccountIdAndPlayId(Long accountId, String playId);
}
