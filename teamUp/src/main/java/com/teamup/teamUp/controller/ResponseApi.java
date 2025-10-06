package com.teamup.teamUp.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApi<T>{
    private String message;
    private T data;
    private Boolean success;
}
