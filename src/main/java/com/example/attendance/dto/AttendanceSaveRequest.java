package com.example.attendance.dto;

import com.example.attendance.entity.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record AttendanceSaveRequest(
        @NotNull(message = "Subject is required") Long subjectId,
        @NotNull(message = "Date is required") LocalDate date,
        @NotEmpty(message = "No attendance records to save") @Valid List<Item> records) {

    /** studentId is the students.id database key. */
    public record Item(@NotNull(message = "Student is required") Long studentId,
                       @NotNull(message = "Status is required") AttendanceStatus status) {
    }
}
