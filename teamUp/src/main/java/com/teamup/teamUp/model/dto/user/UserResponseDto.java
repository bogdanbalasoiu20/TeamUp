package com.teamup.teamUp.model.dto.user;

import com.teamup.teamUp.model.entity.User;

import java.util.UUID;

public record UserResponseDto(UUID id, String email, String username) {
    public static UserResponseDto from(User user){
        return new UserResponseDto(user.getId(), user.getEmail(), user.getUsername());
    }
}
