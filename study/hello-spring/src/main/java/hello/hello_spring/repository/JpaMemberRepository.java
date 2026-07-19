package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class JpaMemberRepository implements MemberRepository{

    // 내부에서 의존성을 알아서 주입한다음 들고있는다. 우리는 여기서 꺼내 쓰기만 하면된다.
    private final EntityManager em;

    // 그러기 위해 em을 주입받는다.
    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Member save(Member member) {
        // persist 영속화, 저장하다.
        em.persist(member); // 개쩐다.
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<Member> findByName(String name) {
        List<Member> result = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();

        return result.stream().findAny();
    }

    @Override
    public List<Member> findAll() {
        // 특별한 jpa query launguage 객체지향 쿼리를 사용하여야한다. sql과 거의 비슷하다.
        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
        return result;
    }
}
