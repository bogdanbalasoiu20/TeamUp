package com.teamup.teamUp.model.dto.user;

import com.teamup.teamUp.model.enums.Position;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequestDto(
        @Past
        LocalDate birthday,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$",
                message = "Phone number must contain only digits and can start with +")
        String phoneNumber,

        Position position,

        @Size(max = 64)
        String city,

        @Size(max = 300)
        String description
        ) {
}
