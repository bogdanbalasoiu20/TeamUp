package com.teamup.teamUp.model.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank
        String emailOrUsername,

        @NotBlank
        String password) {
}
