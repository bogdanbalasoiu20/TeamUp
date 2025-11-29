package com.teamup.teamUp.model.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank (message = "Email or username is required.")
        String emailOrUsername,

        @NotBlank (message = "Password is required.")
        String password) {
}
