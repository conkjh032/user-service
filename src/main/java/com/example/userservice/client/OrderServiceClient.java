package com.example.userservice.client;

import com.example.userservice.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service") // application name
public interface OrderServiceClient {

    @GetMapping("/order-service/{userId}/orders") // 호출하고 싶은 마이크로서비스의 url 그대로 붙여넣기
    List<ResponseOrder> getOrders(@PathVariable("userId") String userId);
    // 위 데이터 형식이 order-service에서의 ResponseEntity<List<ResponseOrder>> 과 다른 이유?
    // ResponseEntity는 HTTP 응답에 대한 전체 내용을 가짐. status code, header, body
    // ResponseEntity로 감싸지 않는다면, body만 전달.
    // 그래서 body 부분인 List<ResponseOrder> 만 사용


}
