package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
// Discovery에 저장할 Client 표시
// 없어도 되지만, autoRegister=false 설정으로 변경될 수 있으므로 명시하는 것이 좋다.
// EnableEurekaCilent : eureka만 지원
// EnableDiscoveryClient : 다른 클라이언트 구현체도 지원원@EnableDiscoveryClient // Discovery에 저장할 Client 표시
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
