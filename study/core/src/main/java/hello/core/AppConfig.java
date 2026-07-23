package hello.core;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 구성 영역 (사용영역에서 사용하는 인스턴스 생성)
@Configuration // 설정 정보 담당 파일 // 주석 처리시 순수한 AppConfig 클래스, Singleton 깨짐
public class AppConfig {
    // Spring Container 에 등록 @Bean
    // @Bean이 붙은 메서드마다 이미 스프링 빈이 존재하면 존재하는 빈을 반환하고,
    // 없으면 생성해서 등록한 뒤 반환하는 코드가 동적으로 만들어진다. 이로써 싱글톤이 보장된다.

    // Service <- memberRepository 필요
    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    // 사용하는 Repository (Memory, DB(미구현))
    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    // OrderService <- Repository, DiscountPolicy 필요
    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    // 사용하는 DiscountPolicy (Fix, Rate 중 택1)
    @Bean
    public DiscountPolicy discountPolicy() {
        // System.out.println("AppConfig.discountPolicy");
        return new RateDiscountPolicy();
    }
}
