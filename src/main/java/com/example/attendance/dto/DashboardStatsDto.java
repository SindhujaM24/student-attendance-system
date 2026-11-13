package com.example.attendance.dto;

public record DashboardStatsDto(long totalStudents, long totalSubjects,
                                long todayMarked, long todayPresent,
                                long studentsBelowMinimum, double minimumPercentage) {
}
