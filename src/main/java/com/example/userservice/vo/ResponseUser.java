package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON에서 null 값 무시하고 받을 수 있게 함
public class ResponseUser {
    private String email;
    private String name;
    private String UserId;

    private List<ResponseOrder> orders;
}
