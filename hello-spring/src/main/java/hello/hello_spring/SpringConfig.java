package hello.hello_spring;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.*;
import hello.hello_spring.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

// 자바 코드로 직접 컴포넌트 스캔, 스프링 빈을 등록하는 방법이다.
@Configuration
public class SpringConfig {

    private final MemberRepository memberRepository;

    @Autowired
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // @PersistenceContext 하면 di 없이 할수있지만 사용하지 않는다.
//    private EntityManager em;
//
//    @Autowired
//    public SpringConfig(EntityManager em) {
//        this.em = em;
//    }

    // 스프링이 뜰때  > @Configuration > memberService() 호출 > 등록
    @Bean
    public MemberService memberServie(){
        return new MemberService(memberRepository);
    }

//    @Bean
//    public MemberRepository memberRepository() {
        // MemberRepository는 인터페이스이다. 객체 생성이 불가능함
        // MemoryMemberRepository()를 요구해야한다.
        // return new MemoryMemberRepository();

        // Jdbc 저장소를 생성하여 객체를 갈아끼운다.
        // 인자로 dataSource 가 필요한데 이를 스프링이 제공해준다.
        // return new JdbcMemberRepository(dataSource);

        // JdbcTemplate를 사용한다.
        // return new JdbcTemplateMemberRepository();

        // jpa멤버리포지토리 + EntityManager 를 사용한다.
        // return new JpaMemberRepository(em);

        // spring data jpa를 사용한다. 이는 jpa에 대한 기본 지식이 없으면 위험한 학습이 된다.

    // }
}
