package com.teamup.teamUp.model.dto.auth;

import com.teamup.teamUp.model.dto.user.UserResponseDto;

public record AuthResponseDto(String token, UserResponseDto userDto) {
}
