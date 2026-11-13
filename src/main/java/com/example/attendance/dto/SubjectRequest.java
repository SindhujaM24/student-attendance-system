package com.example.attendance.dto;

import jakarta.validation.constraints.*;

public record SubjectRequest(
        @NotBlank(message = "Subject ID is required")
        @Size(max = 20, message = "Subject ID can have at most 20 characters")
        String subjectCode,

        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name can have at most 100 characters")
        String name,

        @NotBlank(message = "Department is required")
        String department,

        @NotNull(message = "Semester is required")
        @Min(value = 1, message = "Semester must be between 1 and 8")
        @Max(value = 8, message = "Semester must be between 1 and 8")
        Integer semester) {
}
