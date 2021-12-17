package com.example.userservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component //클래스를 bean으로 등록
@Data
//@AllArgsConstructor // 모든 argument를 가진 생성자 생성
//@NoArgsConstructor // argument 없는 생성자 생성
public class Greeting {

    // application.yaml의 설정값 가져오기
    @Value("${greeting.message}")
    private String message;
}
