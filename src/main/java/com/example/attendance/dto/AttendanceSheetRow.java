package com.example.attendance.dto;

import com.example.attendance.entity.AttendanceStatus;

/** One line of the attendance sheet. status is null when attendance is not marked yet. */
public record AttendanceSheetRow(Long studentId, String studentCode, String studentName, AttendanceStatus status) {
}
