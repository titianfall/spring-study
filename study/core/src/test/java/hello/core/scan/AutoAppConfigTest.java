package hello.core.scan;

import hello.core.AutoAppConfig;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoAppConfigTest {

    @Test
    public void basicScan() {
        // 현재 AutoAppConfig는 아무것도 작성하지 않았다.
        // 1. MemberServiceImpl(@Component) + Constructor(@Autowired)
        // 2. OrderServiceImpl(@Component) + Constructor(@Autowired)
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class);

        MemberService memberService = ac.getBean(MemberService.class);
        OrderService orderService = ac.getBean(OrderService.class);
        MemberRepository memberRepository = ac.getBean(MemberRepository.class);

        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
        assertThat(orderService).isInstanceOf(OrderServiceImpl.class);
        assertThat(memberRepository).isInstanceOf(MemoryMemberRepository.class);

        ac.close();
    }
}
