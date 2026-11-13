package com.example.attendance.dto;

import com.example.attendance.entity.Role;

public record LoginResponse(Long userId, String username, String fullName, Role role) {
}
