package hello.hello_spring.controller;

import hello.hello_spring.domain.Member;
import hello.hello_spring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

// 해당 멤버 컨트롤러 애노테이션을 이 멤버 컨트롤로 객체를 생성해서 스프링에 넣어두고 spring이 관리합니다.
// 스프링 컨테이너에서 객체를 생성해서 들고있는다.
// @Component가 내부적으로 있어 컴포넌트(@Component) 스캔이라고도 한다.
@Controller
public class MemberController {
    // 이걸 new로 할경우 서로 다른 객체로 비교를 하게되므로
    // DI가 필요하다. 이번엔 직접 넣어줄 필요는 없다!
    // @Autowired private MemberService // 를 통해 필드주입도 가능하다.
    private final MemberService memberService;

    // spring container에 등록하고 연결하여 쓰면된다.(alt + insert > Constructor)
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 직접 사이트에 접속할 경우
    @GetMapping("/members/new")
    public String createForm() {
        return "members/createMemberForm";
    }

    // submit 버튼 누를경우
    @PostMapping("/members/new")
    public String create(MemberForm form){
        Member member = new Member();
        member.setName(form.getName());

        memberService.join(member); // 리포지토리에 추가

        return "redirect:/"; // 회원가입이 끝나면 홈으로 되돌려 보낸다.
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);

        return "members/memberList";
    }
}
