package hello.core.singleton;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StatefulServiceTest {

    @Test
    void statefulServiceSingleton() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        // ThreadA: memberA 10000원 주문
        statefulService1.order("memberA", 10000);
        // ThreadB: memberB 20000원 주문
        statefulService2.order("memberB", 20000);

        // ThreadA: memberA 주문 금액 조회
        int price = statefulService1.getPrice();// 10000? x > 20000
        System.out.println(price);

        assertThat(statefulService1.getPrice()).isEqualTo(20000); // 멀티스레드 공유 변수 문제
    }

    static class TestConfig {
        @Bean
        public StatefulService statefulService() {
            return new StatefulService();
        }
    }
}