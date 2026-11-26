# CatHunt 서버 이미지 (멀티스테이지: 소스 → jar 빌드 → 슬림 실행)
# 1단계: 프로젝트 Gradle wrapper(9.9.2, Spring Boot 4.2와 일치)로 bootJar 빌드
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
# gradlew 줄바꿈(CRLF→LF) 정리 + 실행권한 → 테스트 제외하고 실행 가능한 jar 빌드
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# 2단계: 실행(JRE만, 이미지 작게)
FROM eclipse-temurin:17-jre
WORKDIR /app
# 실행 가능한 boot jar 만 복사(-plain.jar 는 glob에 안 걸림)
COPY --from=build /src/build/libs/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
