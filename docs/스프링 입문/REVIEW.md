# 스프링 입문 통합 복습 노트 (1~7장)

> `01. 프로젝트 환경 설정` ~ `07. AOP` 챕터 학습 기록을 하나로 압축한 복습용 문서.
> 기준: Spring Boot `3.5.16` / Java `17` / Gradle Groovy DSL

---

## 전체 그림 — 한 장으로 보는 흐름

```
[1~2장] 웹 기초        브라우저 → 내장 Tomcat → Controller → viewResolver → Thymeleaf → HTML 응답
[3장]   백엔드 설계     Controller → Service → Repository(인터페이스) → 도메인
[4장]   스프링 컨테이너  빈 등록(컴포넌트 스캔 / @Bean) + DI(생성자 주입 권장)
[5장]   웹 MVC 완성    폼(GET) → 등록(POST) → redirect:/ → 목록(th:each)
[6장]   DB 접근        Memory → JDBC → JdbcTemplate → JPA → 스프링 데이터 JPA (설정 한 줄 교체 = OCP)
[7장]   AOP           공통 관심사(시간 측정)를 @Aspect + @Around 프록시로 분리
```

---

## 1장. 프로젝트 환경 설정

### 버전 주의사항
- Boot 3.x = **Java 17 이상** + **`jakarta.*`** 패키지 (`javax.*` 아님). 강의(Boot 2.x) 설정 그대로 복사 금지.
- H2는 Boot 3.x 기준 `2.1.214` 이상.
- IntelliJ에서 **Project SDK와 Gradle JVM 둘 다** JDK 17로 맞춰야 한다.

### starter와 라이브러리
- `spring-boot-starter-web` 하나로 **내장 Tomcat + Spring MVC**가 딸려온다 → 별도 서버 설치 없이 실행 가능.
- 로그는 **SLF4J(API) + Logback(구현체)** 분리 구조 — 구현체를 갈아껴도 코드 불변.

### MVC 동작 흐름 (핵심 암기)

```
브라우저 GET /hello
  → 내장 Tomcat
  → @GetMapping("/hello") HelloController.hello(Model)   ← 진입점
  → return "hello" (뷰 이름)
  → viewResolver: "hello" → classpath:/templates/hello.html
  → Thymeleaf가 th:* 를 데이터로 채워 렌더링
  → 완성된 HTML 응답
```

- **viewResolver**: 뷰 이름 → `templates/{이름}.html` 경로로 조립해주는 컴포넌트.
- **Model**: 컨트롤러 → 뷰로 데이터를 전달하는 그릇. 파라미터에 선언만 하면 스프링이 만들어 넣어줌(= DI의 첫 경험).
- 파일 위치: 동적 템플릿은 `resources/templates/`, 정적 파일은 `resources/static/` (`static/index.html` = 웰컴 페이지).

### 기타 핵심
- **어노테이션** = 코드에 붙이는 메타데이터 표식. 프레임워크가 읽고 대신 처리 (Servlet의 수동 파싱/라우팅 코드를 대체).
- **컴포넌트 스캔 범위** 때문에 패키지는 `@SpringBootApplication` 하위에 둔다.
- 코드 수정 시 서버 재시작 필요한 이유: JVM은 기동 시점의 `.class`를 메모리에 올려두고 계속 씀. → DevTools로 자동 재시작 가능 (`static/`은 재시작 없이 반영).
- HTTP 상태 코드: `2xx` 성공 / `3xx` 리다이렉트 / `4xx` 클라이언트 잘못 / `5xx` 서버 잘못. 매핑 없는 URL = 404, 컨트롤러 예외 = 500.
- 빌드: `gradlew build` → `build/libs/`에 **Fat JAR**(라이브러리 전부 포함, `java -jar`로 실행)와 plain JAR(실행 불가) 생성. 이상하면 `gradlew clean build`.

---

## 2장. 스프링 웹 개발 기초 — 세 가지 방식

| 방식 | 핵심 | 응답 | viewResolver |
| --- | --- | --- | --- |
| 정적 컨텐츠 | `static/` 파일 그대로 | 파일 | 사용 안 함 |
| MVC + 템플릿 | `@Controller` + `return "뷰이름"` | 렌더링된 HTML (SSR) | 사용 |
| API | `@ResponseBody` | JSON / 문자열 | 사용 안 함 |

### 우선순위
**컨트롤러 매핑 > 정적 파일.** 스프링은 먼저 컨트롤러에서 매핑을 찾고, 없을 때만 `static/`을 뒤진다.

### @RequestParam
- 쿼리 파라미터(`?name=spring`)를 메서드 파라미터에 바인딩.
- `required` 기본값이 `true` → 파라미터 없이 요청하면 **400 Bad Request**. `required=false`나 `defaultValue`로 완화.

### @ResponseBody + JSON 자동 변환
```
@ResponseBody + 객체 반환
  → viewResolver 건너뜀
  → HttpMessageConverter 동작
     - String 반환 → StringHttpMessageConverter
     - 객체 반환   → MappingJackson2HttpMessageConverter (Jackson) → JSON
```
- Jackson은 **getter 기준**으로 직렬화 (`getName()` → `"name"` 키). getter/setter = 자바빈 규약.
- JSON = 키-값 텍스트 형식, REST API 사실상 표준 (XML보다 가볍고 읽기 쉬움).

---

## 3장. 회원 관리 예제 — 백엔드 개발

### 계층 구조와 책임

```
컨트롤러(HTTP 수신) → 서비스(비즈니스 로직) → 리포지토리(DB 접근) → DB
                        도메인(데이터 구조)은 계층 간에 오간다
```

각 계층은 자기 역할만 — 한 계층을 고쳐도 다른 계층에 영향 없음.

### 인터페이스 설계 (뒤 장들의 복선)
- 저장소가 아직 미정 → `MemberRepository` **인터페이스** + `MemoryMemberRepository` 구현체.
- DB가 정해지면 구현체만 갈아끼우고 서비스 코드는 그대로 (→ 6장에서 실제로 4번 교체).
- 동시성 주의: 실무라면 `HashMap`→`ConcurrentHashMap`, `long`→`AtomicLong`.

### Optional
- null 대신 감싸서 반환하는 컨테이너. `get()` / `orElse()` / `ifPresent(람다)`.
- 중복 검증 관용구:
```java
memberRepository.findByName(member.getName())
        .ifPresent(m -> { throw new IllegalStateException("이미 존재하는 회원입니다."); });
```

### 테스트 (JUnit + AssertJ)
- **given / when / then** 패턴으로 작성. 테스트 이름은 한글도 OK(의도가 명확해짐).
- **테스트는 서로 독립적**이어야 한다 → `@AfterEach`로 저장소 초기화, `@BeforeEach`로 새 객체 생성.
- 예외 테스트: `assertThrows(IllegalStateException.class, () -> ...)` — 던진 예외 객체를 반환해 메시지까지 검증 가능.

### DI가 왜 필요한지 체감한 순간
서비스가 저장소를 내부에서 `new`하면 테스트와 **서로 다른 인스턴스**를 쓰게 됨.
→ 생성자로 주입받게 리팩토링 → `@BeforeEach`에서 같은 저장소를 서비스에 넣어 공유.

---

## 4장. 스프링 빈과 의존관계

### 빈 등록 두 가지 방법 (섞지 말 것)

| 방법 | 방식 | 언제 |
| --- | --- | --- |
| **컴포넌트 스캔** | `@Controller` / `@Service` / `@Repository` (모두 내부에 `@Component`) | 정형화된 계층 코드 |
| **자바 설정** | `@Configuration` + `@Bean` 메서드 | 구현체를 **교체**해야 할 때 |

- 스캔 범위 = `@SpringBootApplication` 클래스의 **패키지와 하위**만. 밖에 두면 등록 안 됨.
- 스프링 빈은 기본 **싱글톤** — 어디서 주입받아도 같은 인스턴스.

### DI 3가지 방법 — 생성자 주입 권장

```java
private final MemberService memberService;   // final로 불변 보장

@Autowired  // 생성자가 하나면 생략 가능
public MemberController(MemberService memberService) {
    this.memberService = memberService;
}
```

- 필드 주입 ✕ (테스트 어려움), setter 주입 △ (외부 노출), **생성자 주입 ◎** (생성 시점 1회, 불변).

### 트러블슈팅
- `JDK isn't specified for module` → **IDE 설정 문제**. Project Structure에서 SDK 17 지정 + Gradle Reload.
- `required a bean of type '...' that could not be found` → **빈 미등록**. `@Service` 등 붙이거나 `@Bean` 등록. **컴파일 성공 ≠ 실행 성공** — 빈 누락은 런타임에 드러난다.

---

## 5장. 회원 관리 예제 — 웹 MVC 개발

### URL-흐름 요약

```
GET  /             → HomeController.home()         → home.html
GET  /members/new  → MemberController.createForm() → createMemberForm.html
POST /members/new  → MemberController.create()     → memberService.join() → redirect:/
GET  /members      → MemberController.list()       → memberList.html (th:each)
```

### 핵심 포인트
- `@GetMapping("/")`을 만들면 `static/index.html`은 무시된다 (컨트롤러 우선).
- **폼 바인딩**: `<input name="name">` ↔ `MemberForm.setName()` — 폼 필드명과 프로퍼티명이 일치해야 자동 매핑.
- `return "redirect:/"` — 뷰 렌더링 없이 지정 URL로 재요청 (등록 후 홈 이동 패턴).
- 목록 출력: `model.addAttribute("members", list)` → 템플릿에서 `th:each="member : ${members}"` + `th:text="${member.name}"`.

### 트러블슈팅
- `Ambiguous mapping` → **같은 URL을 두 메서드가 매핑**해서 기동 실패. 한 URL = 한 핸들러. 컨트롤러 역할 분리로 해결.

---

## 6장. 스프링 DB 접근 기술

### H2 데이터베이스
- 접속 순서: **최초 1회만** `jdbc:h2:~/test`(파일 생성) → 이후 항상 `jdbc:h2:tcp://localhost/~/test`.
- **파일 락 에러의 원리**:
  - 임베디드 모드 = 프로세스가 DB 파일을 **직접 독점**. 앱 + H2 콘솔 두 프로세스가 동시에 열면 `Locked by another process`.
  - TCP(서버) 모드 = **H2 서버 하나만** 파일을 열고, 클라이언트들은 소켓(9092)으로 접속 → 동시 접근 가능.
  - 한 줄: **임베디드 = 파일 독점(1프로세스), TCP = 서버가 파일을 들고 여럿이 공유.**
- H2 SQL은 **MySQL이 아니라 표준 SQL 계열** 자체 방언. (`generated by default as identity`가 증거, `auto_increment`는 안 통함.) 필요하면 `;MODE=MySQL` 호환 모드.

### JDBC는 항상 바닥에 있다

```
우리 코드 → JPA(Hibernate) / JdbcTemplate / MyBatis → JDBC(java.sql.*) → DB별 드라이버 → DB
```

- JPA를 써도 **JDBC 드라이버(`runtimeOnly 'com.h2database:h2'`)는 항상 필요** — 상위 기술은 JDBC를 대체하는 게 아니라 감싸는 것.
- DB 연결은 클라이언트-서버 모델: DB가 서버, 우리 앱은 **JDBC 드라이버를 낀 클라이언트**.

### 기술 발전 흐름 (이 장의 핵심)

| 단계 | 직접 짜는 것 | 사라진 것 |
| --- | --- | --- |
| 순수 JDBC | Connection/SQL/매핑/자원정리 전부 | — |
| JdbcTemplate | SQL + RowMapper | 연결·예외·자원정리 반복 코드 |
| JPA | 엔티티 매핑 + 일부 JPQL | 기본 SQL |
| 스프링 데이터 JPA | **인터페이스 선언만** | 구현 클래스 자체 |

**매 단계 `SpringConfig`의 빈 등록 한 줄만 교체** — 서비스/컨트롤러는 무수정. 이것이 **OCP**(확장에 열림, 수정에 닫힘)이고, 가능한 이유는 서비스가 구현체가 아닌 **인터페이스에 의존(DIP)** + 스프링 DI가 구현체를 주입하기 때문.

### SOLID 요약
S 단일 책임 / **O 개방-폐쇄(이 장의 주인공)** / L 구현체는 인터페이스 자리에 그대로 치환 가능 / I 인터페이스 잘게 / **D 추상에 의존**.

### 단계별 핵심 메모
- **순수 JDBC**: try-catch-finally + close 반복. "이렇게 고생했구나" 참고용. `spring.datasource.username=sa` 필수(공백 주의).
- **JdbcTemplate**: `RowMapper` = ResultSet 한 행 → 객체 콜백. **RowMapper가 읽는 컬럼과 SELECT 컬럼이 일치**해야 함 (`select name`만 하고 `rs.getLong("id")` 읽으면 `Column "id" not found`).
- `stream().findAny()` = **List → Optional 변환** 관용구 (0개면 empty, 1개면 그 값).
- **JPA**:
  - `@Entity` + `@Id @GeneratedValue(strategy = IDENTITY)` — **IDENTITY = DB 자동증가 컬럼**(H2/MySQL). 오라클 시퀀스는 별도의 `SEQUENCE` 전략.
  - Boot 3.x는 `jakarta.persistence` (단 `javax.sql.DataSource`는 그대로).
  - `EntityManager`는 부트가 자동 생성한 빈을 **생성자 주입**으로 받음 (`@PersistenceContext` 필드 주입 대신 스프링 권장 방식으로 통일).
  - **JPA 데이터 변경은 서비스에 `@Transactional` 필수** (정상 커밋 / 런타임 예외 롤백).
  - JPQL = 테이블이 아닌 **엔티티 대상** 쿼리. 명명 파라미터는 반드시 `:name` (콜론 누락 → `No parameter named ':name'`).
  - `@Bean` 메서드에 `@Autowired` 붙이면 기동 실패 — 의존성은 설정 클래스 **생성자**로 받는다.
- **스프링 데이터 JPA**: `interface ... extends JpaRepository<Member, Long>` — 기본 CRUD 자동 + `findByName()` 같은 **메서드 이름 기반 쿼리 자동 생성**. 구현체가 자동 등록되므로 설정에서는 주입만 받음.

### 통합 테스트
- `@SpringBootTest`(컨테이너 + 실제 DB) + `@Transactional`(**테스트에서는 항상 롤백**).
- 단위 테스트 = 자바 메모리에서 끝남 / 통합 테스트 = **DB까지 실제로 갔다가 롤백으로 치움** (롤백 ≠ 메모리 전용).
- 롤백이 필요한 이유: 커밋되어 남은 데이터가 다음 테스트를 오염시킴 (중복 예외가 엉뚱한 이유로 터지는 등).
- 검증은 **AssertJ `assertThat(actual).isEqualTo(expected)`로 통일 권장** (JUnit `assertEquals`는 인자 순서 혼동 위험, 예외는 `assertThrows` 사용).

### 현업 선택
**기본 = JPA + 스프링 데이터 JPA**, 복잡한 동적 쿼리 = Querydsl, 고난도 쿼리/튜닝 = 네이티브 쿼리·JdbcTemplate. (한국 SI/금융은 MyBatis도 강세. 순수 JDBC는 직접 안 씀.)

---

## 7장. AOP와 시간 측정

### 왜 필요한가
- **핵심 관심 사항**(회원 가입·조회) vs **공통 관심 사항**(시간 측정·로그·트랜잭션·권한).
- 시간 측정을 각 메서드에 직접 넣으면 모든 메서드에 반복 + 핵심 로직 오염 + 변경 시 전부 수정.

### TimeTraceAop

```java
@Aspect
@Component
public class TimeTraceAop {
    @Around("execution(* hello.hello_spring..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("START: " + joinPoint.toString());
        try {
            return joinPoint.proceed();   // 실제 대상 메서드 실행
        } finally {
            System.out.println("END: " + joinPoint + " "
                    + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
```

- `@Aspect` = AOP 클래스 표시, `@Around` = 대상 메서드 전후를 감쌈, `proceed()` = 실제 메서드 실행.

### 동작 원리 — 프록시
```
Controller → [MemberService 프록시] → TimeTraceAop 실행 → 실제 MemberService
```
스프링이 실제 빈 앞에 **프록시**를 세워 실행 시점에 공통 로직을 끼워 넣는다.

### 로그 읽는 법
- 포인트컷이 넓으면(`hello.hello_spring..*`) controller/service/repository **전 계층**에서 START/END가 찍힌다.
- START/END는 **호출 스택처럼 중첩** — 바깥(Controller)부터 START, 안쪽(Repository)부터 END.
- Hibernate SQL 로그(`spring.jpa.show-sql=true`)는 AOP가 아니라 JPA가 찍는 별도 로그.
- 범위 좁히기: `execution(* hello.hello_spring.service..*(..))` (OR 조건으로 여러 패키지 조합 가능).

### 트러블슈팅
- **AOP 로그가 안 찍힘** → ① `@Aspect` 확인 ② 빈 등록 확인 ③ **포인트컷 패키지명이 실제 패키지와 일치하는지** 확인(실제로 밟은 지뢰: `hello.hellospring` vs `hello.hello_spring`).
- 로그가 너무 많음 → 포인트컷 범위 좁히기.

---

## 최종 암기 카드

1. **요청 흐름**: 브라우저 → Tomcat → Controller → (viewResolver → Thymeleaf) 또는 (@ResponseBody → HttpMessageConverter/Jackson).
2. **컨트롤러 매핑 > 정적 파일.**
3. **계층 분리**: Controller / Service / Repository / Domain — 각자 하나의 책임.
4. **빈 등록**: 컴포넌트 스캔(정형) vs @Bean(교체 유연) — 섞지 않기. 빈은 싱글톤.
5. **DI는 생성자 주입** + `final`. 생성자 하나면 `@Autowired` 생략.
6. **인터페이스 + DI = OCP/DIP**: 저장 기술을 Memory→JDBC→JdbcTemplate→JPA→데이터 JPA로 바꿔도 설정 한 줄만 교체.
7. **JDBC 드라이버는 무엇을 쓰든 항상 바닥에** 깔려 있다.
8. **H2 동시 접근은 TCP 모드** — 임베디드는 파일 독점이라 락 충돌.
9. **JPA 변경 작업은 @Transactional**, 테스트의 @Transactional은 **항상 롤백**.
10. **AOP = 공통 관심사 분리** — @Aspect + @Around + 프록시. 포인트컷 패키지명 확인 먼저.
