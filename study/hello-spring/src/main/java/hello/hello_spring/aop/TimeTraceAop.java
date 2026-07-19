package hello.hello_spring.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component // 컴포넌트 스캔을 해도 되며
// SpringConfig에 @Bean으로 등록해도 된다.
public class TimeTraceAop {

    // 타겟팅 문법 - 패키지 하위에 모두 적용
    @Around("execution(* hello.hello_spring.service..*(..))")
    public Object execute(ProceedingJoinPoint jointPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("START: " + jointPoint.toString());
        try{
            // Object result = jointPoint.proceed();
            // return result
            // ctrl + alt + n > inline 함수로
            return jointPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            System.out.println("END: " + jointPoint.toString() + " " + timeMs);
        }
    }
}
