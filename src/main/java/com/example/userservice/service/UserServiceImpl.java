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


    // ?????????: Email(???????????? Id)??? ???????????? ?????? ???????????? ?????????
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // username??? Email??? ??????
        UserEntity userEntity = userRepository.findByEmail(username);

        // Email??? ????????? ?????????, ?????? ??????
        if(userEntity == null){
            throw new UsernameNotFoundException(username);
        }

        // DB?????? Email??? ?????? -> DB??? pwd??? ????????? pwd ?????? -> ?????? -> ????????? ????????? ??? ?????? (????????? ??? ???)
        // Security ???????????? User ???????????? ????????? ?????? ?????? ???
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>()); // ArrayList?????? ????????? ???????????? ??????. ????????? ????????? ??? ArrayList

    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        // UserDto -> UserEntity ??????
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd())); // ???????????? ?????????
        userRepository.save(userEntity);

        return null;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        // ????????? ????????? ?????? ??????
        if(userEntity == null){
            throw new UsernameNotFoundException("User Not Found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        /* Using a restTemplate */
////        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders"; ??? ????????? ?????? ??????
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//
//        // order service??? @GetMapping("/{user_id}/orders")??? ????????? ??? ????????? ????????????
//        ResponseEntity<List<ResponseOrder>> orderListResponse
//                = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                    new ParameterizedTypeReference<List<ResponseOrder>>() { // List<ResponseOrder>??? orderService??? @GetMapping("/{user_id}/orders")??? ????????? ??? ?????? ?????? ?????? ??????
//                });
//
//        // ????????? ??? ????????????
//        List<ResponseOrder> ordersList = orderListResponse.getBody();


        // CircuitBreaker??? ????????? ????????? ????????? ErrorDecoder??? ???????????? ??????
//        /* Using a Feign Client + ErrorDecoder */
//        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);

        /* CircuitBreaker */
        log.info("Before call orders microservice");
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> ordersList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>()); // run(?????? ?????????, ???????????? ??????)
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
