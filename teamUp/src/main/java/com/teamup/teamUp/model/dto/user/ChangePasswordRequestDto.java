package com.teamup.teamUp.model.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "You must introduce your current password")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$",
                message = """
                    The password must have:
                    1. At least 8 characters
                    2. At least one upper case
                    3. At least one digit
                    4. At least one special character
                    """
        )
        String newPassword,

        @NotBlank(message = "Please confirm your new password")
        String confirmNewPassword
) {
    @JsonIgnore
    @AssertTrue(message = "The password confirmation is not matching") //marcheaza o metoda care asteapta true; daca metoda intoarce false -> eroare de validare, altfel totul e ok
    public boolean isConfirmationMatching(){
        return newPassword !=null && newPassword.equals(confirmNewPassword);
    }

    @JsonIgnore
    @AssertTrue(message = "New password must be different from the current password")
    public boolean isDifferentFromCurrent(){
        return currentPassword!=null && !currentPassword.equals(newPassword);
    }
}
