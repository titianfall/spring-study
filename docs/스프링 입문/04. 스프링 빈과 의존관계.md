<!-- learning-issue-id: spring-0004 -->

# 4. 스프링 빈과 의존관계

> 강의자료: `4. 스프링 빈과 의존관계.pdf`  
> 현재 프로젝트 기준: Spring Boot `3.5.16` / Java `17` / Gradle Groovy DSL

---

## 요약

- 컨트롤러가 서비스를, 서비스가 리포지토리를 **필요로 하는 관계**를 스프링이 대신 연결해 준다 — 이것이 **의존관계 설정**.
- 빈을 등록하는 방법은 두 가지: **컴포넌트 스캔**(`@Component` 계열 어노테이션) 과 **자바 코드 직접 등록**(`@Configuration` + `@Bean`).
- DI(의존성 주입)에는 **필드 주입 / setter 주입 / 생성자 주입** 세 가지가 있고, **생성자 주입을 권장**한다.
- 스프링 빈은 기본적으로 **싱글톤**으로 등록된다 (하나만 만들어 공유).
- 오늘 실제로 만난 에러: `JDK isn't specified for module`(IntelliJ 설정), `Parameter 0 ... required a bean ... that could not be found`(빈 미등록).

---

## 컴포넌트 스캔과 자동 의존관계 설정

`@Controller`가 붙은 컨트롤러를 스프링이 객체로 만들어 컨테이너에 넣어두고 관리한다. 컨트롤러가 서비스를 필요로 하면, 스프링이 컨테이너에서 서비스를 찾아 연결해 준다.

### 어노테이션으로 빈 등록하기

```java
@Controller                      // 컨트롤러를 스프링 빈으로 등록
public class MemberController {
    private final MemberService memberService;

    @Autowired                   // 생성자에 스프링 빈(MemberService)을 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

```java
@Service                         // 서비스를 스프링 빈으로 등록
public class MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

```java
@Repository                      // 리포지토리를 스프링 빈으로 등록
public class MemoryMemberRepository implements MemberRepository {
    // ...
}
```

| 어노테이션 | 대상 | 의미 |
| --- | --- | --- |
| `@Controller` | 컨트롤러 | 웹 요청을 받는 빈으로 등록 |
| `@Service` | 서비스 | 비즈니스 로직 빈으로 등록 |
| `@Repository` | 리포지토리 | 데이터 접근 빈으로 등록 |
| `@Autowired` | 생성자/필드/setter | 스프링 컨테이너의 빈을 연결(주입) |

> `@Controller`, `@Service`, `@Repository` 안에는 모두 **`@Component`** 가 들어있다. 그래서 이 방식을 **컴포넌트 스캔**이라고 부른다.

### 컴포넌트 스캔의 범위

```java
@SpringBootApplication           // 이 클래스가 있는 패키지가 스캔 시작점
public class HelloSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpringApplication.class, args);
    }
}
```

- 스프링은 **`@SpringBootApplication`이 선언된 패키지와 그 하위 패키지**만 컴포넌트 스캔한다.
- 그래서 `hello.hellospring` 밖(다른 패키지)에 `@Component`를 두면 스캔되지 않는다.

> 정리하면: "아무 데나 `@Component`를 붙이면 등록되는가?" → **아니다.** 메인 클래스 하위 패키지 안에 있어야 한다.

### 스프링 빈은 싱글톤

스프링은 빈을 등록할 때 기본적으로 **싱글톤**으로 등록한다. 같은 타입의 빈을 여러 곳에서 주입받아도 **모두 같은 인스턴스**다.

```
스프링 컨테이너
 ┌─────────────────────────┐
 │ memberService (1개)      │ ← 컨트롤러가 받는 것도
 │ memberRepository (1개)   │   같은 객체, 공유됨
 └─────────────────────────┘
```

> `new`로 직접 만들면 호출할 때마다 다른 객체가 생긴다. 스프링 빈으로 등록하면 하나만 만들어 공유하므로 메모리 낭비가 없다.

---

## DI(의존성 주입)의 3가지 방법

```java
// 1) 필드 주입 — 비권장 (외부에서 바꿀 수 없어 테스트가 어려움)
@Autowired private MemberService memberService;

// 2) setter 주입 — public 메서드가 노출되는 단점
@Autowired
public void setMemberService(MemberService memberService) {
    this.memberService = memberService;
}

// 3) 생성자 주입 — 권장
private final MemberService memberService;
@Autowired
public MemberController(MemberService memberService) {
    this.memberService = memberService;
}
```

| 방법 | 특징 | 권장 |
| --- | --- | --- |
| 필드 주입 | 코드는 짧지만 변경이 어렵고 테스트가 까다로움 | ✕ |
| setter 주입 | 중간에 바꿀 수 있으나 setter가 외부에 열림 | △ |
| **생성자 주입** | 객체 생성 시점에 **한 번만** 주입, `final`로 불변 보장 | ◎ |

> 의존관계가 실행 중에 바뀌는 일은 거의 없다. 그래서 **생성자 주입**이 가장 안전하다. 생성자가 하나면 `@Autowired`는 생략 가능하다.

---

## 자바 코드로 직접 스프링 빈 등록하기

컴포넌트 스캔 대신, 설정 클래스에서 `@Bean`으로 직접 등록할 수도 있다.

```java
@Configuration                   // 스프링 설정 클래스
public class SpringConfig {

    @Bean                        // 이 메서드의 반환 객체를 스프링 빈으로 등록
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        // MemberRepository는 인터페이스 → 구현체를 반환
        return new MemoryMemberRepository();
    }
}
```

- 스프링이 뜰 때 `@Configuration`을 읽고 → `@Bean` 메서드를 호출 → 반환 객체를 컨테이너에 등록한다.
- `memberService()`가 `memberRepository()`를 호출해 의존관계도 코드로 직접 연결한다.
- 이 방식을 쓰면 `MemberService`, `MemoryMemberRepository`에 붙였던 `@Service`, `@Repository`, `@Autowired`는 **제거**한다 (두 방식을 섞지 않는다).

> `MemberRepository`는 인터페이스라 `new`로 만들 수 없다. 그래서 구현체인 `new MemoryMemberRepository()`를 반환해야 한다.

### 컴포넌트 스캔 vs 자바 설정 — 언제 무엇을?

| 상황 | 권장 방식 |
| --- | --- |
| 정형화된 컨트롤러/서비스/리포지토리 | **컴포넌트 스캔** (`@Service` 등) |
| 구현 클래스를 상황에 따라 **교체**해야 할 때 | **자바 설정** (`@Bean`) |

> 예: 메모리 저장소 → 나중에 DB 저장소로 교체할 때, `SpringConfig`의 `memberRepository()` 반환값만 바꾸면 **다른 코드는 손대지 않아도 된다.** 이 유연함이 자바 설정 방식의 장점이다.

---

## 트러블슈팅 (오늘 실제로 만난 에러)

### 1. `JDK isn't specified for module 'hello-spring.main'`

- **증상**: 컴파일이 안 되고, 그 여파로 `MemberService ... could not be found` 같은 에러가 줄줄이 발생.
- **원인**: IntelliJ가 모듈을 컴파일할 **JDK(Project/Module SDK)** 를 못 찾는 상태. (`.idea` 설정이 사라졌을 때 자주 발생)
- **해결**:
  1. `Ctrl + Alt + Shift + S` (Project Structure) → **Project → SDK** 를 `temurin-17`로 지정
  2. **Modules → hello-spring** 의 Module SDK 확인
  3. 오른쪽 **Gradle 패널 → Reload(🔄)** 로 설정 재생성

> 이 에러는 **코드 문제가 아니라 IDE 설정 문제**다. 코드의 import/패키지가 맞는데도 "클래스를 못 찾는다"고 하면 JDK 설정부터 의심한다.

### 2. `Parameter 0 of constructor ... required a bean of type 'MemberService' that could not be found`

- **증상**: 컴파일은 되는데 **앱 실행 시** 시작하다가 죽음.
- **원인**: `MemberController`가 `MemberService` 빈을 주입받아야 하는데, `MemberService`가 **빈으로 등록되지 않음** (`@Service`도 없고 `@Bean` 등록도 없음).
- **해결**: 둘 중 하나로 등록
  - `MemberService`에 `@Service`, `MemoryMemberRepository`에 `@Repository` 추가 (컴포넌트 스캔)
  - 또는 `SpringConfig`에서 `@Bean`으로 등록 (자바 설정)

> **컴파일 성공 ≠ 실행 성공.** 빈 등록 누락은 컴파일러가 못 잡고, 스프링 컨테이너가 뜨는 **런타임**에서야 드러난다.

---

## 용어 정리

| 용어 | 설명 |
| --- | --- |
| 스프링 빈 | 스프링 컨테이너가 관리하는 객체 |
| 컴포넌트 스캔 | `@Component`(및 `@Controller`/`@Service`/`@Repository`)를 찾아 자동으로 빈 등록 |
| DI (의존성 주입) | 필요한 객체를 직접 생성하지 않고 외부(컨테이너)에서 주입받는 것 |
| `@Autowired` | 컨테이너의 빈을 연결(주입)하라는 표시 |
| `@Configuration` | 스프링 설정 클래스임을 표시 |
| `@Bean` | 메서드 반환 객체를 직접 빈으로 등록 |
| 싱글톤 | 객체를 하나만 생성해 공유하는 방식 (스프링 빈 기본값) |

---

## 확인 체크리스트

- [ ] `@Controller` / `@Service` / `@Repository`로 컴포넌트 스캔 등록해 보기
- [ ] 생성자에 `@Autowired`로 의존관계 주입 확인
- [ ] 컴포넌트 스캔 범위(메인 클래스 하위 패키지) 이해
- [ ] DI 3가지 방법 중 생성자 주입을 권장하는 이유 설명 가능
- [ ] `SpringConfig`로 자바 설정 방식 빈 등록해 보기
- [ ] 두 방식(컴포넌트 스캔 / 자바 설정)을 섞지 않기
- [ ] `bean ... could not be found` 에러를 빈 등록으로 해결해 보기
