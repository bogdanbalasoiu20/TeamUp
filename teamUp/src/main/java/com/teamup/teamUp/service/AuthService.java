package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.ResourceConflictException;
import com.teamup.teamUp.exceptions.UnauthorizedException;
import com.teamup.teamUp.model.dto.auth.AuthResponseDto;
import com.teamup.teamUp.model.dto.auth.LoginRequestDto;
import com.teamup.teamUp.model.dto.auth.RegisterRequestDto;
import com.teamup.teamUp.model.dto.user.*;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    public AuthResponseDto login(LoginRequestDto request){
        var key = request.emailOrUsername().trim();
        var user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(key, key).orElseThrow(()->new UnauthorizedException("Invalid credentials"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid credentials");

        if(user.isDeleted()){
            throw new UnauthorizedException("Invalid credentials");
        }

        var claims = new HashMap<String, Object>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("tokenVersion", user.getTokenVersion() == null ? 0 : user.getTokenVersion());
        claims.put("pwdChangedAt", user.getPasswordChangedAt() == null
                ? 0L
                : user.getPasswordChangedAt().getEpochSecond());

        var token = jwtService.generate(user.getId().toString(), claims);

        return new AuthResponseDto(token, UserResponseDto.from(user));
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request){
        String email = request.email().trim().toLowerCase();
        if(userRepository.existsByEmailIgnoreCase(email))
            throw new ResourceConflictException("Email already exists");

        String username = request.username().trim();
        if(userRepository.existsByUsernameIgnoreCase(username))
            throw new ResourceConflictException("Username already exists");

        var user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .birthday(request.birthday())
                .city(request.city())
                .position(request.position())
                .description(request.description())
                .build();

        try {
            var userSaved = userRepository.save(user);

            var claims = new java.util.HashMap<String, Object>();
            claims.put("username", userSaved.getUsername());
            claims.put("email", userSaved.getEmail());
            claims.put("tokenVersion", userSaved.getTokenVersion() == null ? 0 : userSaved.getTokenVersion());
            claims.put("pwdChangedAt", userSaved.getPasswordChangedAt() == null ? 0L
                    : userSaved.getPasswordChangedAt().getEpochSecond());

            var token = jwtService.generate(
                    userSaved.getId().toString(),
                    claims
            );

            return new AuthResponseDto(token, UserResponseDto.from(userSaved));
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Email or username already exists");
        }

    }
}
