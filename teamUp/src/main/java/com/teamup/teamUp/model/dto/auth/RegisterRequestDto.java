package com.teamup.teamUp.model.dto.auth;

import com.teamup.teamUp.model.enums.Position;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9]+$",
                message = "Username must contain only letters and digits")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max= 100)
        String password,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "Phone number must contain only digits and can start with +"
        )
        String phoneNumber,

        @Past
        LocalDate birthday,

        Position position,

        @Size(max = 64)
        String city,

        @Size(max = 300)
        String description
) {
}
