package com.example.attendance.dto;

/** Attendance of one student in one subject. */
public record AttendanceSummaryDto(Long studentId, String studentCode, String studentName,
                                   Long subjectId, String subjectName,
                                   long totalClasses, long present, long absent,
                                   double percentage, boolean low) {
}
