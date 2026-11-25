package com.hong.cathuntserver.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// account 테이블 접근. 기기 키로 계정을 찾는다.
// JpaRepository를 상속하면 save·findById·findAll 같은 기본 DB 메서드가 공짜로 생긴다(구현 안 해도 됨).
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 메서드 이름만 이렇게 지으면 스프링이 "device_key로 account를 찾는 SQL"을 자동으로 만들어 준다.
    // 있을 수도 없을 수도 있어서 Optional로 받는다(처음 보는 기기면 비어 있음).
    Optional<Account> findByDeviceKey(String deviceKey);
}
