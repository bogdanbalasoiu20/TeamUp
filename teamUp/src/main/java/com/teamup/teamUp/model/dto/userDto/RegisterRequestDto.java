package com.teamup.teamUp.model.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequestDto(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 4, max = 20)
        String username,

        @NotBlank @Size(min = 8, max= 100)
        String password,

        @NotBlank
        String phoneNumber,

        @Past
        LocalDate birthday,

        @Size(max = 30)
        String position,

        @Size(max = 64)
        String city,

        @Size(max = 300)
        String description
) {
}
