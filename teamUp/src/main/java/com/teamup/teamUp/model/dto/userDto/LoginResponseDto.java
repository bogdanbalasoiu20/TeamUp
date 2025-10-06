package com.teamup.teamUp.model.dto.userDto;

import java.util.UUID;

public record LoginResponseDto(
        UUID id,
        String token,
        String email,
        String username
) {
}
