package hello.hello_spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("hello") // get 메서드이다.
    public String hello(Model model){
        model.addAttribute("data","hello!!");
        return "hello";
    }

    // 내장 Tomcat이 수신하여 hello-mvc가 있는지 찾고
    // get 방식이므로 localhost:8080/hello-mvc.html?name=* 형식이 될것이다.
    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam(value = "name", required = true) String name, Model model){
        // 모델에 등록하여 view가 가져갈 수 있도록 만든다.
        model.addAttribute("name", name);
        return "hello-template";
        // 뷰 이름 반환 → viewResolver → Thymeleaf 렌더링 → 클라이언트 응답한다.
        // viewResolver가 Model 데이터로 th 속성을 채워 HTML 완성
        // 완성된 HTML을 Spring이 클라이언트에 응답한다.
    }

    // api 방식 - 문자로 하는 방식
    @GetMapping("hello-string")
    @ResponseBody // http 프로토콜의 body 부에 내용을 직접 넣겠다.
    public String helloString(@RequestParam("name") String name){
        // viewResolver가 없으며 <html> 이런태그도 없다.
        return "hello " + name; // 단순 문자
        // HttpMessageConverter > StringConverter
    }

    // 데이터를 내놓으려는 경우 - api 방식
    @GetMapping("hello-api")
    @ResponseBody // 문자가 아니라 객체임 >
    public Hello helloApi(@RequestParam("name") String name){
        Hello hello = new Hello();

        hello.setName(name);
        return hello; // 객체 > json 스타일로 변경 및 전송
        // HttpMessageConverter > JsonConverter
        // 기본 객체 처리를 하는 아주 대표적인 라이브러리로
        // MappingJackson2HttpMessageConverter(기본), G-son가 있다.
    }
    // HelloController.Hello
    static class Hello{
        private String name;
        // alt + insert > Getter and Setter 선택
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
}
