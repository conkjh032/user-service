package com.example.userservice;

import com.example.userservice.error.FeignErrorDecoder;
import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient // Discovery에 저장할 Client 표시
// EnableDiscoveryClient : 다른 클라이언트 구현체도 지원
// 없어도 되지만, autoRegister=false 설정으로 변경될 수 있으므로 명시하는 것이 좋다.
// EnableEurekaCilent : eureka만 지원
@EnableFeignClients // 마이크로서비스간 통신
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    // Bean 등록

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // RestTemplate
    @Bean
    @LoadBalanced // config service의 user-service.yml에 입력된 url을 관리하기 applicaiton name으로 관리하기 위함
    // 예를 들어, http:127.0.0.1:8000/order-service/%s/orders => http://ORDER-SERVICE/order-service/%s/orders
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Logger.Level feignLoggerLevel(){ // feign library의 Logger.Level을 빈으로 등록
        return Logger.Level.FULL;
    }

    // FeignErrorDecoder를 component로 등록했기 때문에 굳이 bean으로 등록할 필요 없음.
//    @Bean
//    public FeignErrorDecoder feignErrorDecoder(){
//        return new FeignErrorDecoder();
//    }
}
