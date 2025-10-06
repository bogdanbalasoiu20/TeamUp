package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.userDto.LoginRequestDto;
import com.teamup.teamUp.model.dto.userDto.LoginResponseDto;
import com.teamup.teamUp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request){
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(new ResponseApi<>("Login successful", response, true));
    }
}
