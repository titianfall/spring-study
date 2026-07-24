package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 나는 "service"로 하는게 편해! 라고 가정
// OrderServiceImpl 과 MemberServiceImpl 을 돌시에  "service"로 지정
// 자동 빈 삽입이 두개의 빈을 삽입하려다가 ConflictingBeanDefinitionException 발생
// BeanDefinitionStoreException
@Component // ("service")
public class OrderServiceImpl implements OrderService {

//    private final MemberRepository memberRepository = new MemoryMemberRepository();

    // 정액 할인 정책 > 정률 할인 정책에 따른 OrderServiceImpl 수정
    // private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    // private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
    // DIP 위반: OrderServiceImpl 은 DiscountPolicy, FixDiscountPolicy, RateDiscountPolicy에도 의존한다.
    // OCP 위반: 구현체 변경에 다른 클라이언트 코드 수정

    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy; // DIP원칙은 지키게 되나 NPE 발생

    @Autowired // (ac.getBean(MemberRepository.class); ac.getBean(DiscountPolicy);)
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }

    // 테스트 용도
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}
