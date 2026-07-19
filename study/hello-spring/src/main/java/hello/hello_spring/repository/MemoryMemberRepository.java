package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.*;

// @Repository // spring container 에서 리포지토리로 등록한다.
public class MemoryMemberRepository implements MemberRepository{

    // 공유 변수일 경우 ConcurrentHashMap을 사용해야한다. 단순화를 위해 HashMap 사용
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L; // 0, 1, 2... 이런 값을 생성, 동시성 문제를 생각하기 위해서는 AtomicLong을 써야한다.
    @Override
    public Member save(Member member) {
        // 이름은 사용자가 회원가입 할때 입력되며
        member.setId(++sequence); // id는 시스템에 저장시 등록되는 시스템이 정해주는 흐름이다.
        store.put(member.getId(), member); // map(id, member) 형식 저장
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        // id가 없다면 어떻게할것인가? > Optional로 감쌈
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    // test afterEach 에서 호출하는 메서드로 순서가 보장되지 않는 테스트를 보환한다.
    public void clearStore() {
        store.clear(); // store을 싹 비운다.
    }
}
