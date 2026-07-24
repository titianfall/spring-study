package hello.core;

import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration // 설정정보
@ComponentScan
(
//         다른 파일들이 섞이거나,
//         프로젝트 전체 (의존성 포함)를 찾을 필요는 없거나,
//         혹은 특정 패키지만 적용하고 싶을때 유용하게 사용 가능하다.
//        basePackages = "hello.core",
//        basePackageClasses = AutoAppConfig.class, // @ComponentScan(default)
//         수동으로 등록하는 AppConfig 클래스 스캔 제외 - 기존 예제 코드를 최대한 남기고 유지하기 위해
         excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {

    // 수동 빈 vs 자동 빈
    // 수동 빈이 자동 빈을 overriding 합니다.
    // 잡기 어려운 애매한 버그들이 만들어진다.
    @Bean(name = "memoryMemberRepository")
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
