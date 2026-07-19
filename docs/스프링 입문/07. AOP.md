<!-- learning-issue-id: spring-0007 -->

# 7. AOP와 시간 측정 로그

> 강의자료: `7. AOP.pdf`
> 현재 프로젝트 기준: Spring Boot `3.5.16` / Java `17` / Gradle Groovy DSL

---

## 요약

- AOP는 **공통 관심 사항**을 핵심 비즈니스 로직에서 분리하기 위한 기술이다.
- 회원 가입, 회원 조회 같은 핵심 로직에 시간 측정 코드를 직접 넣으면 모든 메서드에 같은 코드가 반복된다.
- `@Aspect` + `@Around`로 시간 측정 로직을 한 곳에 모으면 핵심 로직은 깔끔하게 유지된다.
- 로그가 많이 찍히는 이유는 포인트컷 범위와 실제 요청 흐름 때문이다. `controller`, `service`, `repository`까지 잡으면 계층마다 START/END가 모두 출력된다.

---

## AOP가 필요한 이유

강의에서는 회원 가입과 회원 조회 시간 측정을 예로 든다.

처음에는 `MemberService.join()`과 `MemberService.findMembers()` 안에 직접 시간을 재는 코드를 넣는다.

```java
public Long join(Member member) {
    long start = System.currentTimeMillis();
    try {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    } finally {
        long finish = System.currentTimeMillis();
        long timeMs = finish - start;
        System.out.println("join " + timeMs + "ms");
    }
}
```

이 방식은 동작은 하지만 문제가 있다.

- 회원 가입, 회원 조회의 핵심 로직은 **비즈니스 로직**이다.
- 시간 측정은 여러 메서드에 공통으로 필요한 **공통 관심 사항**이다.
- 둘이 한 메서드에 섞이면 핵심 로직을 읽기 어려워진다.
- 시간 측정 방식을 바꾸려면 여러 메서드를 전부 수정해야 한다.

그래서 시간 측정 같은 코드는 AOP로 빼는 것이 좋다.

---

## 핵심 관심 사항과 공통 관심 사항

| 구분 | 예시 | 설명 |
| --- | --- | --- |
| 핵심 관심 사항 | 회원 가입, 회원 조회, 회원 저장 | 애플리케이션이 실제로 해결하려는 비즈니스 기능 |
| 공통 관심 사항 | 시간 측정, 로그, 트랜잭션, 권한 검사 | 여러 기능에 반복해서 끼어드는 부가 기능 |

AOP는 이 둘을 분리한다.

```text
핵심 로직: MemberService.join(), findMembers()
공통 로직: 실행 시간 측정, START/END 로그 출력
```

---

## TimeTraceAop 기본 형태

강의의 시간 측정 AOP는 대략 이런 형태다.

```java
@Aspect
@Component
public class TimeTraceAop {

    @Around("execution(* hello.hello_spring..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("START: " + joinPoint.toString());
        try {
            return joinPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            System.out.println("END: " + joinPoint.toString() + " " + timeMs + "ms");
        }
    }
}
```

### 코드 의미

| 코드 | 의미 |
| --- | --- |
| `@Aspect` | 이 클래스가 AOP 클래스임을 표시 |
| `@Component` | 컴포넌트 스캔으로 스프링 빈 등록 |
| `@Around(...)` | 대상 메서드 실행 전후를 감싸서 실행 |
| `ProceedingJoinPoint` | 현재 AOP가 감싸고 있는 메서드 정보 |
| `joinPoint.proceed()` | 실제 대상 메서드를 실행 |

`@Around`는 메서드를 감싸는 구조라서 `proceed()` 전에는 START가 찍히고, 대상 메서드가 끝난 뒤 `finally`에서 END가 찍힌다.

---

## 포인트컷 패키지명 주의

현재 프로젝트의 실제 패키지는 `hello.hello_spring`이다.

```java
package hello.hello_spring.aop;
```

따라서 포인트컷도 이 패키지명과 맞아야 한다.

```java
@Around("execution(* hello.hello_spring..*(..))")
```

만약 강의 코드의 `hello.hellospring`을 그대로 쓰면 현재 프로젝트에서는 아무 메서드도 매칭되지 않는다.

```java
@Around("execution(* hello.hellospring..*(..))") // 현재 프로젝트에서는 매칭 안 됨
```

이 경우 `TimeTraceAop`가 빈으로 등록되어 있어도 로그가 찍히지 않는다.

---

## 왜 로그가 많이 찍히나

현재 포인트컷이 넓으면 아래 패키지의 스프링 빈 메서드가 모두 잡힌다.

```java
@Around("execution(* hello.hello_spring..*(..))")
```

즉 `controller`, `service`, `repository`가 모두 대상이 된다.

회원 가입 요청은 단순히 `join()` 하나만 실행되는 게 아니다.

```text
GET /members/new
-> MemberController.createForm()

POST /members/new
-> MemberController.create()
-> MemberService.join()
-> Repository.findByName()   // 중복 회원 검사
-> Repository.save()         // 저장
-> redirect:/

GET /
-> HomeController.home()
```

그래서 실제 로그도 여러 계층에서 찍힌다.

```text
START: execution(String hello.hello_spring.controller.MemberController.createForm())
END: execution(String hello.hello_spring.controller.MemberController.createForm()) 1

START: execution(String hello.hello_spring.controller.MemberController.create(MemberForm))
START: execution(Long hello.hello_spring.service.MemberService.join(Member))
START: execution(Optional hello.hello_spring.repository.SpringDataJpaMemberRepository.findByName(String))
Hibernate: select m1_0.id,m1_0.name from member m1_0 where m1_0.name=?
END: execution(Optional hello.hello_spring.repository.SpringDataJpaMemberRepository.findByName(String)) 157
START: execution(Member hello.hello_spring.repository.MemberRepository.save(Member))
Hibernate: insert into member (name,id) values (?,default)
END: execution(Member hello.hello_spring.repository.MemberRepository.save(Member)) 49
END: execution(Long hello.hello_spring.service.MemberService.join(Member)) 209
END: execution(String hello.hello_spring.controller.MemberController.create(MemberForm)) 230

START: execution(String hello.hello_spring.controller.HomeController.home())
END: execution(String hello.hello_spring.controller.HomeController.home()) 0
```

영상보다 로그가 많아 보이는 이유는 보통 다음 차이다.

- 영상에서는 특정 기능 하나만 보거나 `service` 중심으로 설명했을 수 있다.
- 현재 요청은 `GET 폼 조회 -> POST 가입 -> 중복 검사 -> 저장 -> redirect -> 홈 조회`까지 이어진다.
- 포인트컷이 `hello.hello_spring..*`라서 컨트롤러와 리포지토리까지 모두 잡힌다.

---

## START/END 순서가 중첩되는 이유

AOP는 메서드를 감싸기 때문에 호출이 중첩되면 START는 바깥쪽부터 찍히고 END는 안쪽부터 찍힌다.

```text
Controller START
  Service START
    Repository START
    Repository END
  Service END
Controller END
```

이는 호출 스택과 같다.

```text
MemberController.create()
  -> MemberService.join()
     -> Repository.findByName()
     -> Repository.save()
```

따라서 START/END가 번갈아 완전히 한 쌍씩 끝나는 것이 아니라, 안쪽 메서드가 끝난 뒤 바깥쪽 메서드가 끝나는 식으로 출력된다.

---

## 로그 범위를 줄이고 싶을 때

서비스 계층만 보고 싶으면 포인트컷을 `service` 패키지로 좁힌다.

```java
@Around("execution(* hello.hello_spring.service..*(..))")
```

그러면 `MemberService.join()`이나 `MemberService.findMembers()` 같은 서비스 메서드만 찍힌다.

컨트롤러와 서비스까지만 보고 싶으면 OR 조건을 쓴다.

```java
@Around(
    "execution(* hello.hello_spring.controller..*(..)) || " +
    "execution(* hello.hello_spring.service..*(..))"
)
```

학습할 때는 전체 흐름을 보려고 넓게 잡아도 되고, 실제 확인할 때는 관심 계층만 좁혀 보는 편이 읽기 쉽다.

---

## JPA 로그와 AOP 로그가 같이 보이는 이유

현재 `application.properties`에 다음 설정이 있다.

```properties
spring.jpa.show-sql=true
```

그래서 AOP 로그 사이에 Hibernate SQL 로그도 같이 출력된다.

```text
START: execution(Optional ...findByName(String))
Hibernate: select m1_0.id,m1_0.name from member m1_0 where m1_0.name=?
END: execution(Optional ...findByName(String)) 157
```

이 흐름은 이렇게 읽으면 된다.

```text
Repository.findByName() 시작
-> JPA가 SQL 생성 및 실행
-> Repository.findByName() 종료
```

즉 Hibernate 로그는 AOP가 찍은 게 아니라, JPA가 DB에 보낸 SQL을 보여주는 별도 로그다.

---

## AOP 적용 후 의존관계

AOP가 적용되면 스프링은 실제 빈 앞에 프록시를 세운다.

```text
Controller
  -> MemberService 프록시
      -> TimeTraceAop 실행
      -> 실제 MemberService 실행
```

그래서 컨트롤러가 직접 실제 `MemberService`를 호출하는 것처럼 보여도, 중간에 프록시가 끼어 공통 로직을 실행할 수 있다.

강의의 핵심은 이것이다.

- 핵심 로직은 `MemberService`에 남긴다.
- 시간 측정은 `TimeTraceAop`로 뺀다.
- 스프링 AOP 프록시가 둘을 실행 시점에 연결한다.

---

## 트러블슈팅

### AOP 로그가 안 찍힘

확인 순서:

1. `@Aspect`가 붙어 있는가?
2. `@Component` 또는 `@Bean`으로 스프링 빈 등록이 되었는가?
3. `@Around` 포인트컷의 패키지명이 실제 패키지와 맞는가?
4. 필요한 AOP 관련 의존성이 런타임에 들어와 있는가?

이번 프로젝트에서 실제로 밟은 부분은 3번이었다.

```java
// 잘못된 패키지명
@Around("execution(* hello.hellospring..*(..))")

// 현재 프로젝트에 맞는 패키지명
@Around("execution(* hello.hello_spring..*(..))")
```

### 로그가 너무 많이 찍힘

원인:

```java
@Around("execution(* hello.hello_spring..*(..))")
```

이 포인트컷은 하위 패키지 전체를 대상으로 한다.

해결:

```java
@Around("execution(* hello.hello_spring.service..*(..))")
```

서비스만 보고 싶으면 범위를 좁힌다.

---

## 확인 체크리스트

- [ ] AOP가 필요한 이유를 핵심 관심 사항과 공통 관심 사항으로 구분해서 설명 가능
- [ ] `@Aspect`, `@Around`, `ProceedingJoinPoint`, `proceed()`의 역할 설명 가능
- [ ] 포인트컷 패키지명이 실제 프로젝트 패키지와 일치하는지 확인
- [ ] `/members/new` 가입 요청이 `GET -> POST -> 중복 검사 -> 저장 -> redirect -> GET /` 순서로 흐르는 것 설명 가능
- [ ] START/END 로그가 호출 스택 구조로 중첩되어 찍히는 이유 설명 가능
- [ ] 로그가 많을 때 포인트컷을 `service` 패키지 등으로 좁혀서 조절 가능
