package com.hong.cathuntserver.config;

import com.hong.cathuntserver.auth.CurrentAccountIdResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// @CurrentAccountId 리졸버 등록 + CORS 설정.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    // app.cors.allowed-origins(쉼표 구분)를 읽는다. 모바일 클라는 CORS가 필요 없어 기본은 비어 있음(=교차 출처 차단).
    public WebConfig(@Value("${app.cors.allowed-origins:}") String origins) {
        this.allowedOrigins = (origins == null || origins.isBlank())
            ? new String[0]
            : origins.split("\\s*,\\s*");
    }

    // 우리가 만든 @CurrentAccountId 리졸버를 스프링 MVC에 등록한다. 이게 있어야 컨트롤러에서 @CurrentAccountId가 동작한다.
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentAccountIdResolver());
    }

    // 허용 도메인이 지정됐을 때만 CORS를 연다. 비어 있으면(모바일 전용) 아무 교차 출처도 허용하지 않는다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0) return;
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
