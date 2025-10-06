package com.teamup.teamUp.service;

import com.teamup.teamUp.mapper.UserMapper;
import com.teamup.teamUp.model.dto.userDto.LoginRequestDto;
import com.teamup.teamUp.model.dto.userDto.LoginResponseDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDto login(LoginRequestDto request){
        var key = request.emailOrUsername().trim();
        var user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(key, key).orElseThrow(()->new BadCredentialsException("Invalid credentials"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");

        var token = jwtService.generate(user.getId().toString(), Map.of("username", user.getUsername()));

        return new LoginResponseDto(user.getId(),token,user.getEmail(),user.getUsername());
    }
}
