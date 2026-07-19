package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MemoryMemberRepositoryTest {

    MemoryMemberRepository repository = new MemoryMemberRepository();

    // 테스트가 끝날때마다 리포지토리를 깔끔하게 정리해주는 코드를 넣어야 한다.
    // save, findByName, * 등등 끝날때마다 실행하는 콜백 메서드이다
    @AfterEach
    public void afterEach(){
        repository.clearStore(); // 저장소 삭제 > 순서 상관이 없어짐
    }

    @Test
    public void save(){
        Member member = new Member();
        member.setName("spring");

        repository.save(member);

        // result와 member가 동일하다면 저장에 문제가 없다는 뜻이다.
        Member result = repository.findById(member.getId()).get();
        // junit이 제공하는 import, 두 객체는 Object actual 타입이다.
        // Assertions.assertEquals(result, null);

        // assertj.core.api 를 사용하면 더 편리하게 쓸수있다.
        // import static 을 통해 Assertions.을 생략했다.
        // assertThat(actual).isEqualTo(expected)
        assertThat(member).isEqualTo(result);
    }

    @Test
    public void findByName(){
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);

        // shift + f6을 통해 중복되는 이름을 한번에 변경이 가능하다.
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);

        // 실행 단축키는 ctrl shift f10
        Member result1 = repository.findByName("spring1").get();
        assertThat(result1).isEqualTo(member1);

        Member result2 = repository.findByName("spring2").get();
        assertThat(result2).isEqualTo(member2);
    }

    // 테스트 순서는 보장되지 않음에 주의해야한다.
    @Test
    public void findAll(){
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);

        List<Member> result1 = repository.findAll();
        assertThat(result1.size()).isEqualTo(2);
    }
}
