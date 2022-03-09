package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 인증 및 권한 관련 설정
 */

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private Environment env;
    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public WebSecurity(Environment env, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    // 권한 관련 메소드
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests().antMatchers("/actuator/**").permitAll();

        // 해당 url로 들어온 요청은 인증없이 서비스 사용 가능 설정
        // 참고 : https://jhhan009.tistory.com/31
        http.authorizeRequests().antMatchers("/**").permitAll();
        http.addFilter(getAuthenticationFilter());

        http.headers().frameOptions().disable(); // h2 db에 접근이 안됨
    }


    // 인증 관련 메소드
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // DB에서 입력된 Email에 맞는 pwd를 가져와야 한다. 유저 관련 비즈니스 로직은 UserService에 있다.
        // DB에서 가져온 pwd(encrypted)와 입력된 pwd(not encrypted)를 비교한다.
        // bCryptPasswordEncoder 넣어줘서 입력된 pwd(not encrypted) 암호화하여 비교한다.
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);

        super.configure(auth);
    }


    // AuthenticationFilter 사용
    private AuthenticationFilter getAuthenticationFilter() throws Exception{
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager(), userService, env);
//        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }
}
