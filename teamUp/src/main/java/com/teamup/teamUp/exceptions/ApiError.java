package com.teamup.teamUp.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ApiError {
    private final boolean success;
    private final ErrorDetails error;
    private final String timestamp;
    private final String path;

    public ApiError(String code, String message, List<FieldErrorDetail> details, String path) {
        this.success = false;
        this.error = new ErrorDetails(code, message, details);
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }


    public record ErrorDetails(String code, String message, List<FieldErrorDetail> details) {
    }
}
