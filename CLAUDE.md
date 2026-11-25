# CatHunt 서버 (catHuntServer)

고양이 쥐잡기 게임의 백엔드. Unity 클라이언트가 HTTPS + REST + JSON으로 접속한다.
무엇을 만들지는 Unity 저장소의 `Assets/Docs/PRD.pdf` 9장, 세팅 순서는 `Assets/Docs/SERVER_SETUP.md`를 따른다.
이 저장소는 **Unity 저장소(`C:\hong\CatHunt`)와 분리된 별도 repo**다.

## 기술 환경

- 언어: Java 17
- 프레임워크: **Spring Boot 4.2.0** (Spring Framework 7). 스타터 이름이 3.x와 다름: `spring-boot-starter-webmvc`(=구 `-web`), `spring-boot-starter-webmvc-test`.
- 빌드: Gradle (Groovy DSL), wrapper는 Gradle 9.9.2
- DB: **PostgreSQL 18** (로컬). DB `cathunt`, 소유자/접속 유저 `hong`
- 토큰: JWT (jjwt 0.12.6)
- 베이스 패키지: `com.hong.cathuntserver`

## 접속 정보 (개발)

- DB URL: `jdbc:postgresql://localhost:5432/cathunt`
- 유저: `hong` / 비번은 **환경변수 `DB_PASSWORD`** (application.yaml에 개발 기본값이 있으나 운영은 환경변수로 덮는다)
- 서버 포트: 기본 8080. (로컬에서 8080을 다른 프로세스가 쓰면 `--server.port=8081`로 띄운다)

## 설계 규칙 (루트 CLAUDE.md·PRD 9장과 일치)

- **젤리·뽑기·구매·보상은 서버가 계산한다.** 클라이언트가 보낸 재화·결과 값은 믿지 않는다.
- **계정은 기기 키(UUID)로 만든다.** OS 고유값(deviceUniqueIdentifier 등)은 안 쓴다. 첫 기기 키면 새 계정, 있으면 로그인.
- **비밀값은 코드에 박지 않는다.** JWT 시크릿(`JWT_SECRET`), DB 비번(`DB_PASSWORD`)은 환경변수. 서버 주소·키를 커밋에 넣지 않는다.
- **한 판 결과는 시작 때 받은 플레이 ID로 보고하고, 멱등 키로 한 번만 반영한다.**
- **가격·확률·보상표는 서버 기준 데이터(`GET /master`)가 원본.**
- DB 스키마: 개발은 JPA `ddl-auto=update`, 운영 전환 시 **Flyway**로 관리.

## 출시 심사 대비 (앱스토어/구글플레이)

클라이언트·서버 둘 다 아래를 지킨다(나중에 심사 반려 안 나게):
- **개인정보 최소 수집.** 기기 고유값·광고 ID·위치 안 쓴다. 계정 식별은 앱이 만든 UUID(기기 키)뿐.
- **운영은 반드시 HTTPS.** 평문 HTTP는 로컬 개발만.
- **계정/데이터 삭제 경로 제공**(구글·애플 요구). 추후 `DELETE /me` 같은 삭제 API와 클라 설정 메뉴를 만든다.
- **비밀값·키 하드코딩 금지**, 로그에 개인정보·토큰 원문 남기지 않기.
- 서버가 재화를 계산하므로 클라 조작으로 부당 이득 불가(심사와 별개로 치팅 방지).

## 패키지 구조

```
com.hong.cathuntserver
  ├─ CatHuntServerApplication.java
  ├─ account/
  │   ├─ Account.java              (account 테이블: device_key unique, nickname, representative_cat)
  │   ├─ AccountRepository.java
  │   ├─ GuestLoginService.java    (로그인+새 계정 프로비저닝: 지갑·시작 고양이 생성)
  │   └─ AuthController.java       (POST /auth/guest)
  ├─ auth/
  │   ├─ TokenService.java         (JWT 발급·검증)
  │   └─ dto/ (GuestLoginRequest, GuestLoginResponse)
  ├─ catalog/                      ── 마스터(공통) ──
  │   ├─ CatDef.java               (cat_def: 전체 고양이 카탈로그. code=Unity id와 동일)
  │   ├─ Rarity.java               (COMMON/RARE/LEGEND, 순서 0/1/2 = 클라와 동일)
  │   ├─ CatDefRepository.java
  │   └─ CatCatalogSeeder.java     (시작 때 12마리 시드)
  ├─ wallet/                       ── 계정별 ──
  │   ├─ Wallet.java               (wallet: id=account_id, paw_jelly·exp·level)
  │   └─ WalletRepository.java
  ├─ cat/                          ── 계정별 ──
  │   ├─ OwnedCat.java             (owned_cat: 보유 고양이 + custom_name. (account,cat) unique)
  │   └─ OwnedCatRepository.java
  ├─ deco/                         ── 고양이 꾸미기(스키마만, 엔드포인트 추후) ──
  │   ├─ DecoSlot.java             (HEAD/FACE/NECK)
  │   ├─ DecorationDef.java        (decoration_def: 장식 카탈로그 마스터)
  │   ├─ OwnedDecoration.java      (owned_decoration: 보유 장식)
  │   └─ CatEquip.java             (cat_equip: 고양이별 자리별 착용. (account,cat,slot) unique)
  ├─ furniture/                    ── 방 꾸미기(스키마만, 엔드포인트 추후) ──
  │   ├─ FurnitureDef.java         (furniture_def: 가구 카탈로그 마스터)
  │   └─ OwnedFurniture.java       (owned_furniture: 보유 가구)
  └─ room/
      └─ RoomPlacement.java        (room_placement: 방 안 가구 배치 좌표)
```

## 데이터 모델 요약

- **마스터(모두 공통, GET /master):** `cat_def`(시드됨), `decoration_def`·`furniture_def`(아직 비어 있음, 아트 정해지면 시드). 가격·확률·보상표도 추후 여기로.
- **계정별(GET /me):** `account`(대표 고양이·닉네임) + `wallet`(재화) + `owned_cat`(보유+별명) + 꾸미기(`owned_decoration`·`cat_equip`·`owned_furniture`·`room_placement`).
- 새 계정 시작값은 `application.yaml`의 `app.new-account.*`(분홍젤리·레벨·시작 고양이). 코드에 안 박음.

## 현재 상태 / 다음 할 일

- **done:** `POST /auth/guest`. 기기 키 처음이면 **계정+지갑+시작 고양이(cheese=대표)** 생성, 있으면 로그인. JWT 토큰 발급. 고양이 카탈로그 12마리 DB 시드.
- **done:** 인증 — `BearerAuthFilter`가 `Authorization: Bearer <token>`에서 accountId를 꺼내 request 속성에 담고, 컨트롤러는 `@CurrentAccountId long accountId`로 받는다(없거나 틀리면 401). (`auth/CurrentAccountId`·`CurrentAccountIdResolver`·`config/WebConfig`)
- **done:** `GET /me` — 토큰으로 account+wallet+owned_cat(카탈로그 이름·등급 포함)을 묶어 반환. curl로 토큰 있음→200·없음/가짜→401 확인. 한글 이름 UTF-8 정상.
- **done:** `GET /master` — 기준 데이터(뽑기 cost·dupeRefundPercent·weights + cat_def 12마리) 반환. 토큰 없이 접근(공통 데이터). 뽑기 수치는 `application.yaml`의 `app.gacha.*`를 `GachaProperties`(@ConfigurationProperties)로 바인딩(코드에 안 박음). 값은 클라 GachaConfig와 동일(100 / 50% / 70·25·5).
- **done:** `POST /gacha/draw`(토큰 필요) — 서버가 **비용 차감·등급 확률(70/25/5)·중복환급(50%)**을 계산. **멱등**: 요청 본문 `requestKey`로 재전송 시 `gacha_request`에 저장된 결과를 그대로 반환(중복 차감 방지). 부족하면 아무것도 안 바꾸고 400(요청 키도 소모 안 함). curl로 신규/중복/환급/멱등 재전송(지갑 불변)/부족 400 전부 확인. 로직은 `GachaService`(rollRarity·pickOne·refund).
- **done:** 한 판 보고 — `POST /play/start`(토큰)가 플레이 ID(UUID) 발급·`play_session` STARTED 저장. `POST /play/report`(토큰, body: playId·caughtCount)가 **보상(발바닥=catches×5, exp=catches×4)을 서버가 계산**해 지갑 반영 + 레벨업(`Leveling`, expTable=app.play.exp-to-next-level=[100], 클라 로직과 동일). **멱등**: 이미 REPORTED면 저장 결과만 반환(이중 지급 방지). 잘못된 playId→404, caughtCount 음수→400(@Min), `maxCatch`(500) 초과는 깎음(치트 방어). 수치는 `app.play.*`(코드에 안 박음). curl로 보상·레벨업·멱등·404·상한 전부 확인(12번째 테이블 play_session).
- **done:** 프로필 쓰기 `POST /me/{nickname,representative,catname}`(me/ProfileController, 응답 MeResponse) + **`DELETE /me`**(계정+전 데이터 삭제, 출시 심사 필수. account/AccountDeletionService는 EntityManager로 계정별 테이블 전부 지우고 account 삭제, cat_def 등 마스터는 보존). Unity와 연결·Play로 검증 완료.
- **done:** Unity 저장소를 `ServerStore`로 교체 — 로비·뽑기·계정삭제가 서버로 동작(Play+DB 확인).
- **다음:** 꾸미기 구매/착용 API(decoration/furniture/room 엔티티는 이미 있음).
- **클라(Unity) 게스트 로그인 UX:** 앱 시작 시 저장된 UUID로 `POST /auth/guest` **자동 호출**(버튼 아님) → 토큰 저장 → `isNew`면 온보딩(첫 고양이·이름), 아니면 로비 직행. 401이면 UUID로 재로그인 1회 후 재시도. (게스트는 앱 삭제 시 계정 유실 — 계정 연동은 MVP 밖)
- 꾸미기(deco/furniture/room)는 **테이블·엔티티만 만들어 둠**. 아트·가격 정해지면 카탈로그 시드 + 구매/착용/배치 엔드포인트 추가.
- **done:** 출시 보안/안정 1차. **① 동시 로그인 보강** — `GuestLoginService`를 레이스 안전하게 재작성(쓰기는 `@Transactional` 메서드 provisionNew/touchExisting를 `ObjectProvider` 자기참조로 호출, 바깥 `login`에서 `DataIntegrityViolationException` 잡아 먼저 만들어진 계정으로 재로그인). **② 공통 에러 포맷** — `common/GlobalExceptionHandler`(@RestControllerAdvice)가 모든 예외를 `{code,message}`(`common/ApiError`)로 통일(ResponseStatusException→상태코드명, @Valid 실패→VALIDATION, 그 외→INTERNAL + 상세는 로그만). **③ 비밀값 분리** — `application.yaml`에서 DB 비번·JWT 시크릿 기본값 제거(env/프로필로만), 개발값은 `application-local.yaml`(.gitignore), 운영은 `application-prod.yaml`(ddl-auto=validate·show-sql=false·Flyway on, 비밀값은 환경변수). 기본 활성 프로필 `local`. **④ CORS** — `WebConfig`에 `app.cors.allowed-origins`로 제어(기본 비어 교차출처 차단, 모바일은 불필요). **⑤ Flyway** — `flyway-core`+`flyway-database-postgresql` 추가, `db/migration/V1__init.sql`(pg_dump 베이스라인, 12테이블), 개발은 Flyway 끄고(ddl-auto) 운영은 baseline-on-migrate로 V1부터. `compileJava` 성공. ⚠️ **HTTPS는 배포 영역** — 리버스 프록시(nginx/Caddy)로 TLS 종료 + 클라 `ServerConfig` 운영주소 https(에디터 경고 가드 추가). ★ 운영 배포 때 환경변수(DB_PASSWORD·JWT_SECRET·DB_URL·CORS_ALLOWED_ORIGINS)와 `SPRING_PROFILES_ACTIVE=prod` 세팅 필요.

## 실행

```
# 개발 서버 실행
.\gradlew.bat bootRun

# 게스트 로그인 테스트
curl -X POST http://localhost:8080/auth/guest -H "Content-Type: application/json" -d "{\"deviceKey\":\"test-uuid-0001\"}"
```
