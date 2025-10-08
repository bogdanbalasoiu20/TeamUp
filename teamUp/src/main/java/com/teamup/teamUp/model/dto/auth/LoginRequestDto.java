package com.teamup.teamUp.model.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank (message = "This field is required")
        String emailOrUsername,

        @NotBlank (message = "This field is required")
        String password) {
}
