package com.example.attendance.dto;

public record StudentResponse(Long id, String studentCode, String name, String email, String phone,
                              String department, Integer year, String section) {
}
