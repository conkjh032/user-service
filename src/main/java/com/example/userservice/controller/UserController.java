package com.example.userservice.controller;

import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserServiceImpl;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.dto.UserDto;
import com.example.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.userservice.service.UserService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RestController // RESTful 웹 서비스 위해서. 객체 데이터를 JSON 형식으로 HTTP로 전송
@RequestMapping("/")
public class UserController {

    // application.yaml에 있는 설정 가져오는 방법 1 : vo(Value Object)에 주입하여 빈으로 등록하고 Autowired로 받기
    @Autowired
    private Greeting greeting;

    // application.yaml에 있는 설정 가져오는 방법 2 : Autowired와 생성자로 변수에 주입
    private Environment env;
    private UserService userService;

    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s", env.getProperty("local.server.port"));
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return env.getProperty("greeting.message"); // 생성자로 설정값 가져오기
        return greeting.getMessage(); // @Value로 설정값 가져오기
    }

    // ResponseEntity : Response Status를 설정
    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user){

        // RequestUser -> UserDto 변환
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        // userDto -> ResponseUser 변환하여 결과 값으로 반환
        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        // 어떤 정보가 저장되었는지 반환하여 보여줄 수 있다.
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser); // Status : 201, Body : ResponseUser
    }

    // 모든 유저 조회
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers(){
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 유저 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){ // PathVariable : userId를 받아서 매개변수로 건내줌
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser responseUser = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

}
