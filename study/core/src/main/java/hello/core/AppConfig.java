package hello.core;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;

// 구성 영역 (사용영역에서 사용하는 인스턴스 생성)
public class AppConfig {

    // 사용하는 Repository (Memory, DB(미구현))
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    // 사용하는 DiscountPolicy (Fix, Rate 중 택1)
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }

    // Service <- memberRepository 필요
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    // OrderService <- Repository, DiscountPolicy 필요
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }


}
