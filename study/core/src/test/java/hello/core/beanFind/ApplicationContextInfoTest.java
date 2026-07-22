package hello.core.beanFind;

import hello.core.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextInfoTest {

    // 이걸 스프링 컨테이너라고 생각하면 된다.
    // 여기에 spring Bean들이 등록된다 (!중복되면 안된다 )
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig .class);

    @Test
    @DisplayName("모든 빈 출력하기")
    void findAllBean() throws Exception {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();

        for(String beanDefinitionName : beanDefinitionNames) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("name = " + bean + " object = " + bean);
        }


    }

    @Test
    @DisplayName("애플리케이션 빈 출력하기")
    void findApplicationBean() throws Exception {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for(String beanDefinitionName : beanDefinitionNames) {
//            Object bean = ac.getBean(beanDefinitionName);
//            System.out.println("name = " + bean + " object = " + bean);
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

            // 애플리케이션 개발을 위해 등록한 빈일 경우에 출력
            if(beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("name = " + beanDefinitionName + " object = " + bean);
            }

        }


    }

}
