package com.hong.cathuntserver.auth;

import java.lang.annotation.*;

// 컨트롤러 메서드 파라미터에 붙이면 토큰에서 꺼낸 accountId를 넣어 준다. 없으면 401.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentAccountId {
}
