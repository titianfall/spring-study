package hello.hello_spring;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import hello.hello_spring.repository.MemoryMemberRepository;
import hello.hello_spring.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 자바 코드로 직접 컴포넌트 스캔, 스프링 빈을 등록하는 방법이다.
@Configuration
public class SpringConfig {

    // 기본적으로 의존성 주입이
    // MemberController 에서 MemberService가
    // MemberService가 MemberRepository가 필요하다.

    // 스프링이 뜰때  > @Configuration > memberService() 호출 > 등록
    @Bean
    public MemberService memberServie(){
        // ctrl + p 를 통해 항상 파라미터를 확인하자
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        // MemberRepository는 인터페이스이다. 객체 생성이 불가능함
        // MemoryMemberRepository()를 요구해야한다.
        return new MemoryMemberRepository();
    }
}
