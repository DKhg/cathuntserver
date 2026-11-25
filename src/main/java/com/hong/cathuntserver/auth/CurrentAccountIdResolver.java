package com.hong.cathuntserver.auth;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

// @CurrentAccountId long accountId 파라미터를 채운다. BearerAuthFilter가 담아둔 속성을 읽고, 없으면 401.
public class CurrentAccountIdResolver implements HandlerMethodArgumentResolver {

    // 이 리졸버가 처리할 파라미터인지 판단한다: @CurrentAccountId가 붙은 long/Long일 때만 true.
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentAccountId.class)
            && (parameter.getParameterType() == Long.class || parameter.getParameterType() == long.class);
    }

    // 실제로 파라미터 값을 만들어 넣는다. 필터가 담아 둔 accountId 속성을 꺼내고, 없으면(= 로그인 안 됨) 401을 던진다.
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object accountId = request != null ? request.getAttribute(BearerAuthFilter.ATTR_ACCOUNT_ID) : null;
        if (accountId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return accountId;
    }
}
