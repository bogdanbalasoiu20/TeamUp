package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.ResourceConflictException;
import com.teamup.teamUp.exceptions.UnauthorizedException;
import com.teamup.teamUp.model.dto.auth.AuthResponseDto;
import com.teamup.teamUp.model.dto.auth.LoginRequestDto;
import com.teamup.teamUp.model.dto.auth.RegisterRequestDto;
import com.teamup.teamUp.model.dto.user.*;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.UserRole;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PlayerCardStatsRepository playerCardStatsRepository;
    private final PlayerBehaviorStatsRepository playerBehaviorStatsRepository;


    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, PlayerCardStatsRepository playerCardStatsRepository, PlayerBehaviorStatsRepository playerBehaviorStatsRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.playerCardStatsRepository = playerCardStatsRepository;
        this.playerBehaviorStatsRepository = playerBehaviorStatsRepository;
    }

    public AuthResponseDto login(LoginRequestDto request){
        var key = request.emailOrUsername().trim();
        var user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(key, key)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid credentials");

        if (user.isDeleted())
            throw new UnauthorizedException("Invalid credentials");

        var claims = new HashMap<String, Object>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        String roleNameLogin = (user.getRole() == null ? "USER" : user.getRole().name());
        claims.put("role", roleNameLogin);
        claims.put("tokenVersion", user.getTokenVersion() == null ? 0 : user.getTokenVersion());
        if (user.getPasswordChangedAt() != null) {
            claims.put("pwdChangedAt", user.getPasswordChangedAt().getEpochSecond());
        }
        var token = jwtService.generate(user.getUsername(), claims);
        return new AuthResponseDto(token, UserResponseDto.from(user));
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request){
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email))
            throw new ResourceConflictException("Email already exists");

        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username))
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
                .role(UserRole.USER)
                .tokenVersion(0)
                .build();

        var saved = userRepository.saveAndFlush(user);

        PlayerCardStats initialCard = PlayerCardStats.builder()
                .userId(saved.getId())
                .pace(68.0)
                .shooting(68.0)
                .passing(68.0)
                .defending(68.0)
                .dribbling(68.0)
                .physical(68.0)
                .gkDiving(68.0)
                .gkHandling(68.0)
                .gkKicking(68.0)
                .gkReflexes(68.0)
                .gkSpeed(68.0)
                .gkPositioning(68.0)
                .overallRating(68.0)
                .lastUpdated(Instant.now())
                .build();

        playerCardStatsRepository.save(initialCard);

        PlayerBehaviorStats stats = PlayerBehaviorStats.builder()
                .user(saved)
                .fairPlay(70.0)
                .competitiveness(70.0)
                .communication(70.0)
                .fun(70.0)
                .selfishness(70.0)
                .aggressiveness(70.0)
                .feedbackCount(0)
                .build();

        playerBehaviorStatsRepository.save(stats);



        var userSaved = userRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("User just saved not found"));

        var claims = new HashMap<String, Object>();
        claims.put("username", userSaved.getUsername());
        claims.put("email", userSaved.getEmail());
        String roleNameReg = (userSaved.getRole() == null ? "USER" : userSaved.getRole().name());
        claims.put("role", roleNameReg);
        claims.put("tokenVersion", userSaved.getTokenVersion() == null ? 0 : userSaved.getTokenVersion());
        if (userSaved.getPasswordChangedAt() != null) {
            claims.put("pwdChangedAt", userSaved.getPasswordChangedAt().getEpochSecond());
        }
        var token = jwtService.generate(userSaved.getUsername(), claims);

        return new AuthResponseDto(token, UserResponseDto.from(userSaved));
    }
}

