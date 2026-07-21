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
@Configuration // 설정 정보 담당 파일
public class AppConfig {
    // spring container 에 등록 @Beam

    // 사용하는 Repository (Memory, DB(미구현))
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    // 사용하는 DiscountPolicy (Fix, Rate 중 택1)
    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }

    // Service <- memberRepository 필요
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    // OrderService <- Repository, DiscountPolicy 필요
    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
}
