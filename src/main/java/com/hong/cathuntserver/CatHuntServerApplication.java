package com.hong.cathuntserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan   // @ConfigurationProperties(app.gacha.*) 자동 등록
public class CatHuntServerApplication {

    // 프로그램 시작점. Spring Boot를 켜면 내장 톰캣이 뜨고(기본 8080), @Component·@Service·@RestController가
    // 자동으로 등록되며, JPA가 DB에 연결된다. 이 메서드 하나로 서버 전체가 기동한다.
    public static void main(String[] args) {
        SpringApplication.run(CatHuntServerApplication.class, args);
    }

}
