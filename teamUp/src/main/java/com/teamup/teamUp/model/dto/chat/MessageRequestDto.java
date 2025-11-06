package com.teamup.teamUp.model.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageRequestDto(
        @NotBlank @NotNull
        String content
) {
}
