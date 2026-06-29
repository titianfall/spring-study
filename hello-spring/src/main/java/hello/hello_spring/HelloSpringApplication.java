package hello.hello_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 아무데나 @Component가 있어도 되는가? >> X
// SpringBootApplication이 선언된 하위 패키지만
// 스프링이 컴포넌트 스캔을 진행하여 싱글톤으로 등록한다.
@SpringBootApplication
public class HelloSpringApplication {
	public static void main(String[] args) {

		SpringApplication.run(HelloSpringApplication.class, args);
	}

}
