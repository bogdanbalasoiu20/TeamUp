package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.userDto.LoginResponseDto;
import com.teamup.teamUp.model.entity.User;

public class UserMapper {
    public static LoginResponseDto toLoginResponseDto(User user, String token) {
        return new LoginResponseDto(user.getId(),token,user.getEmail(), user.getUsername());
    }
}
