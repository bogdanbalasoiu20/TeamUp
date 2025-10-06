package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.userDto.AuthResponseDto;
import com.teamup.teamUp.model.dto.userDto.LoginRequestDto;
import com.teamup.teamUp.model.dto.userDto.RegisterRequestDto;
import com.teamup.teamUp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseApi<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request){
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(new ResponseApi<>("Login successful", response, true));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseApi<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request){
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>("Register successful", response, true));
    }
}
