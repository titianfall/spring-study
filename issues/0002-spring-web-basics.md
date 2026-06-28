<!-- learning-issue-id: spring-0002 -->

# 2. 스프링 웹 개발 기초

> 강의자료: `2. 스프링 웹 개발 기초.pdf`  
> 현재 프로젝트 기준: Spring Boot `3.5.16` / Java `17` / Gradle Groovy DSL

---

## 스프링 웹 개발 세 가지 방식

Spring에서 웹 요청을 처리하는 방식은 크게 세 가지다.

| 방식 | 핵심 어노테이션 | 응답 형태 |
| --- | --- | --- |
| 정적 컨텐츠 | 없음 (파일 그대로) | 파일 그대로 전달 |
| MVC + 템플릿 엔진 | `@Controller` + `return "뷰이름"` | 서버에서 렌더링한 HTML |
| API | `@ResponseBody` | JSON (또는 문자열) |

---

## 정적 컨텐츠

`resources/static/` 아래에 파일을 두면 Spring이 컨트롤러를 거치지 않고 파일 그대로 내려준다.

### 동작 흐름

```
브라우저 GET /hello-static.html
    → 내장 Tomcat 수신
    → Spring: @GetMapping("/hello-static.html") 매핑 있나? → 없음
    → static/ 폴더에서 hello-static.html 탐색
    → 파일 그대로 응답
```

> 컨트롤러 매핑이 정적 파일보다 우선순위가 높다. 같은 이름의 컨트롤러 매핑이 있으면 그쪽이 먼저 실행된다.

---

## MVC와 템플릿 엔진

### Model 1 방식 (MVC 이전)

MVC 이전에는 JSP 하나에 비즈니스 로직과 화면 출력을 모두 작성하는 **Model 1** 방식을 썼다.

```
브라우저 요청 → JSP (DB 조회 + HTML 생성 + 출력) → 응답
```

파일 하나가 너무 많은 일을 해서 코드가 길어질수록 읽기 어렵고, 디자이너와 개발자가 같은 파일을 건드려야 해서 협업이 힘들었다.

### MVC 방식 — 역할과 책임 분리

Model 1의 문제를 해결하기 위해 **역할과 책임(Separation of Concerns)** 을 기준으로 세 가지로 쪼갠 것이 MVC다.

> 하나의 클래스(파일)는 하나의 책임만 진다. 화면을 그리는 코드와 비즈니스 로직이 섞이면 양쪽 모두 수정하기 어려워진다.

**MVC**: Model, View, Controller의 약자. 역할을 세 가지로 분리한다.

| 역할 | 담당 | 책임 |
| --- | --- | --- |
| Model | `Model` 객체 | 데이터 운반. 컨트롤러 → 뷰로 전달하는 그릇 |
| View | `templates/*.html` | 화면 렌더링만. 비즈니스 로직 없이 표시만 담당 |
| Controller | `@Controller` 클래스 | HTTP 요청 수신, 처리 지시, 뷰 이름 반환 |

View에 비즈니스 로직이 없어야 하는 이유는 화면 코드(HTML/CSS)와 처리 코드(Java)를 각각 독립적으로 수정할 수 있어야 하기 때문이다. Controller를 바꿔도 View는 그대로이고, View 디자인을 바꿔도 Controller는 영향받지 않는다.

### 쿼리 파라미터로 데이터 받기

**컨트롤러**

```java
@GetMapping("hello-mvc")
public String helloMvc(@RequestParam("name") String name, Model model) {
    model.addAttribute("name", name);
    return "hello-template";
}
```

**템플릿** (`resources/templates/hello-template.html`)

```html
<html xmlns:th="http://www.thymeleaf.org">
<body>
<p th:text="'hello ' + ${name}">hello! empty</p>
</body>
</html>
```

**요청 예시**

```
GET http://localhost:8080/hello-mvc?name=spring
→ <p>hello spring</p>
```

### return "hello-template" 이 하는 일

`return "hello-template"`은 뷰 이름을 Spring에 돌려주는 것만 한다. 렌더링과 응답은 그 이후 Spring/Thymeleaf가 이어받는다.

```
return "hello-template"
    → viewResolver 가 "templates/hello-template.html" 경로로 변환
    → Thymeleaf 가 Model 데이터로 th:* 속성을 채워 HTML 완성  ← 렌더링
    → 완성된 HTML 을 Spring(내장 Tomcat)이 클라이언트에 응답
```

| 단계 | 담당 | 하는 일 |
| --- | --- | --- |
| 뷰 이름 → 파일 경로 변환 | viewResolver | `"hello-template"` → `templates/hello-template.html` |
| th:* 채워서 HTML 완성 | Thymeleaf | **렌더링** — 템플릿 + 데이터 → 완성된 HTML |
| 완성된 HTML → 클라이언트 전송 | Spring (내장 Tomcat) | HTTP 응답으로 전달 |

컨트롤러는 `return "hello-template"`으로 역할이 끝난다. 렌더링과 응답은 View(Thymeleaf)와 Spring이 담당한다.

**렌더링(Rendering)이란?**

템플릿(뼈대 HTML)과 데이터를 합쳐서 **최종 출력물(완성된 HTML)을 만드는 과정**이다.  
`hello-template.html`의 `${name}` 자리표시자에 `"spring"`을 채워 `<p>hello spring</p>`으로 완성하는 것이 렌더링이다.

```
템플릿: <p th:text="'hello ' + ${name}">hello! empty</p>
데이터: name = "spring"
         ↓ 렌더링
결과:  <p>hello spring</p>
```

### @RequestParam이란?

HTTP 요청의 쿼리 파라미터(`?key=value`)를 메서드 파라미터에 바인딩하는 어노테이션이다.

```
http://localhost:8080/hello-mvc?name=spring
                                 ↑
                           @RequestParam("name") String name 으로 들어옴
```

| 속성 | 설명 |
| --- | --- |
| `required = true` (기본값) | 파라미터가 없으면 400 오류 |
| `required = false` | 파라미터 없어도 null로 받음 |
| `defaultValue = "값"` | 파라미터 없을 때 기본값 적용 |

**`localhost:8080/hello-mvc` 만 치면 왜 에러가 나나?**

`@RequestParam("name")`의 `required` 기본값이 `true`이기 때문이다.  
`?name=...`이 없으면 Spring이 필수 파라미터를 찾지 못해 **400 Bad Request**를 반환한다.

```
GET /hello-mvc          → 400 Bad Request  (name 파라미터 없음)
GET /hello-mvc?name=spring → 200 OK
```

파라미터 없이도 동작하게 하려면 `required = false` 또는 `defaultValue`를 지정한다.

```java
@RequestParam(value = "name", required = false, defaultValue = "guest") String name
```

---

## API 방식

### @ResponseBody란?

컨트롤러 메서드에 `@ResponseBody`를 붙이면 반환값을 **뷰 이름이 아닌 HTTP 응답 본문(body)** 으로 직접 내보낸다.

```java
@GetMapping("hello-string")
@ResponseBody
public String helloString(@RequestParam("name") String name) {
    return "hello " + name;  // 뷰 이름이 아닌 문자열 그대로 응답
}
```

viewResolver가 동작하지 않는다. Thymeleaf 없이 응답이 바로 나간다.

### 객체를 JSON으로 응답하기

```java
@GetMapping("hello-api")
@ResponseBody
public Hello helloApi(@RequestParam("name") String name) {
    Hello hello = new Hello();
    hello.setName(name);
    return hello;  // 객체를 반환하면 JSON으로 자동 변환
}

static class Hello {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**결과**: `{"name":"spring"}`

### JSON 변환이 자동으로 되는 이유

`@ResponseBody`로 **객체**가 반환되면 Spring의 `HttpMessageConverter`가 JSON으로 변환한다.

```
@ResponseBody + 객체 반환
    → HttpMessageConverter 동작
    → MappingJackson2HttpMessageConverter (Jackson 라이브러리)
    → 객체 → JSON 문자열 변환
    → 응답 body에 담아 전송
```

`spring-boot-starter-web`에 Jackson이 기본 포함되어 있어 별도 설정 없이 동작한다.

| 반환 타입 | 동작하는 Converter |
| --- | --- |
| `String` 등 기본 타입 | `StringHttpMessageConverter` |
| 객체 (POJO) | `MappingJackson2HttpMessageConverter` |

객체 → JSON 변환에 쓰이는 대표 라이브러리 두 가지:

| 라이브러리 | 설명 |
| --- | --- |
| **Jackson** (`MappingJackson2HttpMessageConverter`) | Spring Boot 기본 내장. `spring-boot-starter-web` 추가 시 자동 포함. 설정 없이 바로 동작 |
| **Gson** | Google이 만든 JSON 라이브러리. Jackson보다 가볍고 단순하지만 Spring Boot 기본값이 아니라 별도 의존성 추가 필요 |

Spring Boot에서 별도 설정 없이 `@ResponseBody`로 객체를 반환하면 Jackson이 동작한다.

### @ResponseBody 동작 흐름

```
브라우저 GET /hello-api?name=spring
    → HelloController.helloApi() 실행
    → @ResponseBody 감지 → viewResolver 호출 안 함
    → HttpMessageConverter 동작
    → Hello 객체 → {"name":"spring"} JSON 변환
    → HTTP 응답 body에 담아 전송
```

---

## 개념 정리

### JSON이란?

**JavaScript Object Notation**의 줄임말로, 데이터를 키-값 쌍으로 표현하는 텍스트 형식이다.  
언어에 종속되지 않아 서버-클라이언트 간 데이터 교환 표준으로 널리 쓰인다.

```json
{
  "name": "spring",
  "age": 10
}
```

- `"키": 값` 형태, 키는 반드시 큰따옴표
- 값 타입: 문자열(`"spring"`), 숫자(`10`), 불리언(`true/false`), 배열(`[]`), 객체(`{}`), null
- Spring에서 `@ResponseBody` + 객체 반환 시 Jackson 라이브러리가 자동으로 Java 객체 → JSON 문자열로 변환한다

**XML vs JSON**

XML은 JSON 이전에 많이 쓰이던 데이터 교환 형식이다.

```xml
<!-- XML -->
<person>
  <name>spring</name>
  <age>10</age>
</person>
```

```json
// JSON
{ "name": "spring", "age": 10 }
```

| 항목 | XML | JSON |
| --- | --- | --- |
| 형식 | 여는 태그 + 닫는 태그 | 키-값 쌍 |
| 용량 | 태그가 반복되어 상대적으로 무거움 | 간결해서 가벼움 |
| 가독성 | 태그가 많아 복잡해 보임 | 읽기 쉬움 |
| 현재 사용 | 설정 파일(Maven `pom.xml` 등)에 여전히 사용 | REST API 데이터 교환 표준 |

REST API에서는 JSON이 사실상 표준이다. Spring도 `@ResponseBody`로 객체를 반환하면 기본적으로 JSON으로 변환한다.

### getter / setter (자바빈 규약)

Jackson이 객체를 JSON으로 변환할 때 **getter 메서드**를 사용한다.

```java
// getName() → "name" 키로 JSON 직렬화
public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

`get`/`set` 접두사를 제거하고 소문자로 시작하는 이름이 JSON 키가 된다.  
필드가 `private`이어도 getter만 있으면 직렬화된다.

### MVC vs API 방식 비교

| 항목 | MVC + 템플릿 엔진 | API (@ResponseBody) |
| --- | --- | --- |
| 용도 | 서버에서 HTML 완성해서 전달 | 데이터만 전달 (프론트에서 화면 구성) |
| 응답 형태 | 완성된 HTML | JSON / 문자열 |
| viewResolver | 사용함 | 사용 안 함 |
| 주로 쓰이는 곳 | 서버 사이드 렌더링(SSR) | REST API, 모바일 앱 백엔드 |

---

## IntelliJ 단축키

| 단축키 | 기능 |
| --- | --- |
| `Ctrl + P` | 메서드 호출 괄호 안에서 매개변수 목록 팝업 |
| `Alt + Insert` | 코드 생성 메뉴 (Getter/Setter, 생성자, toString 등) |

---

## 확인 체크리스트

- [ ] `static/` 파일 직접 접근 동작 확인
- [ ] `@RequestParam`으로 쿼리 파라미터 받기 실습
- [ ] `@ResponseBody`로 문자열 응답 확인
- [ ] `@ResponseBody`로 객체 반환 시 JSON 변환 확인
- [ ] MVC vs API 방식 차이 이해
- [ ] 최종 HTML 정리본 생성
