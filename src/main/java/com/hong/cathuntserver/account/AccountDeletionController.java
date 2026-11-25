package com.hong.cathuntserver.account;

import com.hong.cathuntserver.auth.CurrentAccountId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 계정 삭제 API. 토큰 필요(자기 계정만 지움). 성공하면 204(내용 없음).
@RestController
@RequestMapping("/me")
public class AccountDeletionController {

    private final AccountDeletionService deletion;

    public AccountDeletionController(AccountDeletionService deletion) {
        this.deletion = deletion;
    }

    // DELETE /me — 내 계정과 모든 데이터를 지운다. 이후 클라는 기기 키·토큰을 지워 새 게스트로 시작한다.
    @DeleteMapping
    public ResponseEntity<Void> deleteMe(@CurrentAccountId long accountId) {
        deletion.delete(accountId);
        return ResponseEntity.noContent().build();
    }
}
