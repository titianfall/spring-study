package hello.hello_spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 접속 우선순위를 엿볼 수 있다.
    // mapping 된것이 있기때문에
    // > 컨트롤러 호출 및 종료(static/index/html)은 무시된다.
    @GetMapping("/")
    public String home() {
        return "home";
    }

//    @GetMapping("/members/new")
//    public String createHome(){
//        return "/members/createMemberForm";
//    }
}
