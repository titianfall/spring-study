package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import hello.hello_spring.repository.MemoryMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

// MemberController 에서 spring container 가 연결해주어야 하는데
// 해당 클래스는 순수 자바 코드이다. 때문에
// @Service // spring에 올라올때 spring container에 멤버 서비스로 등록해준다.
public class MemberService {

    private final MemberRepository memberRepository;

    // DI : Dependency Injection (의존성 주입)
    // @Autowired
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    /**
     * 회원 가입
     * 같은 이름이 있으면 안된다.
     */
    public Long join(Member member){
        // ctrl alt v 누르면 리턴을 작성해준다.
        // 그러나 Optional 반환을 할경우 안이뻐진다.
        // Optional<Member> result = memberRepository.findByName(member.getName());

        // 원래는 if(result == null) 이렇게 분기를 했을것임
        // null일 경우 Optional 내부에 존재하는 메소드를 활용함

        // ctrl t > extract method
        return validateDuplicateMember(member);
    }

    private Long validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 전체 회원 조회
     * 비즈니스 로직은 기능과 밀접하게 관련되도록 작성하여야한다.
     * 역시나 테스트 코드를 작성하기 바란다.
     */
    public List<Member> findMembers(){
        // 리턴 타입을 확인하는 습관을 가지자
        return memberRepository.findAll();
    }

    public Optional<Member> findOne(Long memberId){
        return memberRepository.findById(memberId);
    }
}
