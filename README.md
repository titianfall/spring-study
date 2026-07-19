# Spring-study

김영한 스프링 강의를 따라가며 만든 실습 코드와 학습 기록 저장소.

- **1단계**: 스프링 입문 - 코드로 배우는 스프링 부트, 웹 MVC, DB 접근 기술 ✅ 완료
- **2단계**: 스프링 핵심 원리 - 기본편 🚧 진행 예정

## 학습 정리 진행 상황

### 스프링 입문 (완료)

📌 **[스프링 입문 통합 복습 노트](docs/스프링%20입문/REVIEW.md)** — 1~7장 전체 압축 요약
실습 코드: [`study/hello-spring`](study/hello-spring)

<details>
<summary><b>챕터별 정리</b> (01~07장 완료)</summary>

| # | 챕터 | 정리 |
|---|------|------|
| 01 | 프로젝트 환경 설정 | [01. 프로젝트 환경 설정.md](docs/스프링%20입문/01.%20프로젝트%20환경%20설정.md) |
| 02 | 스프링 웹 개발 기초 | [02. 스프링 웹 개발 기초.md](docs/스프링%20입문/02.%20스프링%20웹%20개발%20기초.md) |
| 03 | 회원 관리 예제 - 백엔드 개발 | [03. 회원 관리 예제 - 백엔드 개발.md](docs/스프링%20입문/03.%20회원%20관리%20예제%20-%20백엔드%20개발.md) |
| 04 | 스프링 빈과 의존관계 | [04. 스프링 빈과 의존관계.md](docs/스프링%20입문/04.%20스프링%20빈과%20의존관계.md) |
| 05 | 회원 관리 예제 - 웹 MVC 개발 | [05. 회원 관리 예제 - 웹 MVC 개발.md](docs/스프링%20입문/05.%20회원%20관리%20예제%20-%20웹%20MVC%20개발.md) |
| 06 | 스프링 DB 접근 기술 | [06. 스프링 DB 접근 기술.md](docs/스프링%20입문/06.%20스프링%20DB%20접근%20기술.md) |
| 07 | AOP | [07. AOP.md](docs/스프링%20입문/07.%20AOP.md) |

</details>

### 스프링 핵심 원리 - 기본편 (진행 예정)

실습 코드: [`study/core`](study/core)
챕터 인덱스: [docs/스프링 핵심 원리](docs/스프링%20핵심%20원리)

## 개발 환경

각 실습 프로젝트는 자체 Gradle Wrapper와 `settings.gradle`을 가진 **독립 빌드**다. 서로 다른 Spring Boot 버전을 써도 각자의 래퍼로 실행하므로 설정이 섞이지 않는다. (IDE에서는 `study/hello-spring`, `study/core`를 각각 별도 프로젝트로 연다.)

### 스프링 입문 — `study/hello-spring`

| 항목 | 버전 |
|------|------|
| Spring Boot | 3.5.16 |
| Java | 17 |
| 빌드 | Gradle (Groovy) |
| DB | H2 2.x |
| 주요 의존성 | web, thymeleaf, data-jpa, h2 |

```bash
cd study/hello-spring
./gradlew bootRun     # Windows: gradlew.bat bootRun
```

### 스프링 핵심 원리 - 기본편 — `study/core`

| 항목 | 버전 |
|------|------|
| Spring Boot | 4.1.0 |
| Java | 17 |
| 빌드 | Gradle (Groovy) |
| 주요 의존성 | spring-boot-starter |

```bash
cd study/core
./gradlew build       # Windows: gradlew.bat build
```

## 저장소 구성

| 경로 | 설명 |
|------|------|
| `docs/<강좌명>/` | 강좌별 학습 정리 Markdown (구 `issues/`) |
| `study/` | 강좌별 독립 실습 프로젝트 |
| `강의자료/` | 강의 PDF (루트 `.gitignore`로 Git 추적 제외) |

## 학습 기록 흐름

1. 강의자료 PDF를 기준으로 `docs/<강좌명>/NN. 챕터 제목.md`에 학습 내용을 **미리 정리**한다.
2. 학습하며 실습 코드를 챕터 단위로 커밋한다.
3. 브랜치에서 커밋 내용을 근거로 md를 보충·보완한 뒤 main에 merge한다.
4. 강좌 전체가 끝나면 통합 복습 노트(`REVIEW.md`)로 압축한다.
