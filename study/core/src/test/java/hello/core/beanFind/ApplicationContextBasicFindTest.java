package hello.core.beanFind;

import hello.core.AppConfig;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApplicationContextBasicFindTest {

    AnnotationConfigApplicationContext ac = new  AnnotationConfigApplicationContext(AppConfig.class);

    // 인터페이스로 조회
    @Test
    @DisplayName("빈 이름으로 조회")
    void findBeanByName() {
        MemberService memberService = ac.getBean("memberService", MemberService.class);

        // memberService 가 MemberServiceImpl의 객체이면 성공
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("이름 없이 타입으로만 조회")
    void findBeanByType() {
        MemberService memberService = ac.getBean(MemberService.class);

        // memberService 가 MemberServiceImpl의 객체이면 성공
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }


    // 구체 타입으로 조회(구현에 의존한 나쁜 예) - 역할과 구현을 분리해야 한다.
    @Test
    @DisplayName("구체 타입으로 조회")
    void findBeanByName2() {
        MemberServiceImpl memberService = ac.getBean("memberService", MemberServiceImpl.class);

        // memberService 가 MemberServiceImpl의 객체이면 성공
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("빈 이름으로 조회X")
    void findBeanByNameX() {
        // No been named 'xxx' available
//        MemberService xxx = ac.getBean("xxx", MemberService.class); // -> NoSuchBeanDefinitionException

        assertThatThrownBy(() -> ac.getBean("xxx", MemberService.class))
            .isInstanceOf(NoSuchBeanDefinitionException.class);
    }
}
