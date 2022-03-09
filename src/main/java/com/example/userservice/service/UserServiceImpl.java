package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    // RestTemplate
    Environment env;
    RestTemplate restTemplate;

    // Feign
    OrderServiceClient orderServiceClient;

    // CircuitBreaker
    CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           Environment env,
                           RestTemplate restTemplate,
                           OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.restTemplate = restTemplate;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }


    // 로그인: Email(여기서는 Id)를 이용해서 계정 체크하는 메소드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // username은 Email로 규정
        UserEntity userEntity = userRepository.findByEmail(username);

        // Email로 찾아도 없으면, 예외 반환
        if(userEntity == null){
            throw new UsernameNotFoundException(username);
        }

        // DB에서 Email로 조회 -> DB의 pwd와 입력된 pwd 비교 -> 인증 -> 검색된 사용자 값 반환 (여기서 할 일)
        // Security 패키지의 User 클래스에 반환할 값을 넣어 줌
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>()); // ArrayList에는 권한을 넣어주면 된다. 지금은 없어서 빈 ArrayList

    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        // UserDto -> UserEntity 변환
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd())); // 비밀번호 암호화
        userRepository.save(userEntity);

        return null;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        // 조회할 유저가 없을 경우
        if(userEntity == null){
            throw new UsernameNotFoundException("User Not Found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        /* Using a restTemplate */
////        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders"; 를 아래와 같이 사용
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//
//        // order service의 @GetMapping("/{user_id}/orders")가 반환한 거 그대로 가져오기
//        ResponseEntity<List<ResponseOrder>> orderListResponse
//                = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                    new ParameterizedTypeReference<List<ResponseOrder>>() { // List<ResponseOrder>는 orderService의 @GetMapping("/{user_id}/orders")로 들어올 때 반환 값과 같은 형식
//                });
//
//        // 반환한 값 추가하기
//        List<ResponseOrder> ordersList = orderListResponse.getBody();


        // CircuitBreaker를 사용할 것이기 때문에 ErrorDecoder를 사용하지 않음
//        /* Using a Feign Client + ErrorDecoder */
//        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);

        /* CircuitBreaker */
        log.info("Before call orders microservice");
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> ordersList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>()); // run(호출 메소드, 에러나면 대처)
        log.info("After called orders microservice");

        userDto.setOrders(ordersList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String username) {
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null){
            throw new UsernameNotFoundException(username);
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        return userDto;
    }
}
