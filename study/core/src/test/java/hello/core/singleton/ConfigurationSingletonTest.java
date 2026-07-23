package hello.core.singleton;

import hello.core.AppConfig;
import hello.core.member.MemberRepository;
import hello.core.member.MemberServiceImpl;
import hello.core.order.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationSingletonTest {

    @Test
    void configureationTest() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        // MemberServiceImpl > MemoryMemberRespoitory 객체 생성
        MemberServiceImpl memberService = ac.getBean(MemberServiceImpl.class);
        // OrderServiceImpl > MemoryMemberRepository(과연 생성하는가?), DiscountPolicy 객체 생성
        OrderServiceImpl orderService = ac.getBean(OrderServiceImpl.class);
        MemberRepository memberRepository = ac.getBean("memberRepository", MemberRepository.class);

        MemberRepository memberRepository1 = memberService.getMemberRepository();
        MemberRepository memberRepository2 = orderService.getMemberRepository();

        System.out.println("MemberService -> memberRepository = " + memberRepository1);
        System.out.println("OrderService -> memberRepository = " + memberRepository2);
        System.out.println("MemoryMemberRepository = " + memberRepository);

        assertThat(memberService.getMemberRepository()).isSameAs(orderService.getMemberRepository());
    }

    @Test
    void configurationDeep() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig been = ac.getBean(AppConfig.class);

        // bean = class hello.core.AppConfig$$SpringCGLIB$$0
        System.out.println("been = " + been.getClass());
    }
}
