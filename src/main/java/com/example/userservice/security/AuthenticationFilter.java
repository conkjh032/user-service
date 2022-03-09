package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * WebSecurity.java에서 로그인 처리할 때 사용
 */

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserService userService,
                                Environment env) {
        super.setAuthenticationManager(authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    // '인증 요청을 보내면 처리'와 '성공할 때' 메소드 필요

    // 로그인 시도 처리
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

       try {
           // Request를 request.getInputStream()으로 받고, RequestLogin 형태로 변경
           // why? POST 방식의 Request은 getInputStream()으로 바꿔주어야 처리할 수 있다.
           RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

           // UsernamePasswordAuthenticationToken으로 Email과 Password를 Token으로 바꿔준다.
           // getAuthenticationManager에서 UserDetailsService의 loadUserByUsername에서 가져온 정보와
           // 여기서 만든 Token을 비교하여 인증
           return getAuthenticationManager().authenticate(
                   new UsernamePasswordAuthenticationToken(
                           creds.getEmail(),
                           creds.getPassword(),
                           new ArrayList<>()
                   )
           );
       } catch (IOException e){
            throw new RuntimeException(e);
       }

    }

    // 인증에 성공하여 로그인할 때, 어떤 값을 반환해줄 것인가. ex) 기간있는 token
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

       String username = ((User)authResult.getPrincipal()).getUsername();
       UserDto userDetails = userService.getUserDetailsByEmail(username);

       // user_id 토큰화
       String token = Jwts.builder()
                    .setSubject(userDetails.getUserId()) // 토큰화 대상
                    .setExpiration(new Date(System.currentTimeMillis() +
                                        Long.parseLong(env.getProperty("token.expiration_time")))) // 만료 기간
                    .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret")) // 토큰화 알고리즘
                    .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());
    }
}
