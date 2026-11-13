package com.example.attendance.dto;

import com.example.attendance.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "Username / email is required") String username,
        @NotBlank(message = "Password is required") String password,
        @NotNull(message = "Please select a login type") Role role) {
}
