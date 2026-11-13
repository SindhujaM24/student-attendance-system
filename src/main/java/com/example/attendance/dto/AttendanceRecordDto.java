package com.example.attendance.dto;

import com.example.attendance.entity.AttendanceStatus;

import java.time.LocalDate;

public record AttendanceRecordDto(LocalDate date, String subjectName, AttendanceStatus status) {
}
