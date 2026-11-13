package com.example.attendance.dto;

import java.util.List;

/** Everything the student dashboard needs about attendance. */
public record StudentAttendanceDto(double overallPercentage, long totalClasses, long present, long absent,
                                   double minimumPercentage,
                                   List<AttendanceSummaryDto> subjects,
                                   List<AttendanceRecordDto> recent) {
}
