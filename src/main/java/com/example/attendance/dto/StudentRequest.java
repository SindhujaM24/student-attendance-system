package com.example.attendance.dto;

import jakarta.validation.constraints.*;

public record StudentRequest(
        @NotBlank(message = "Student ID is required")
        @Size(max = 20, message = "Student ID can have at most 20 characters")
        String studentCode,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name can have at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
        String phone,

        @NotBlank(message = "Department is required")
        String department,

        @NotNull(message = "Year is required")
        @Min(value = 1, message = "Year must be between 1 and 4")
        @Max(value = 4, message = "Year must be between 1 and 4")
        Integer year,

        @NotBlank(message = "Section is required")
        String section) {
}
