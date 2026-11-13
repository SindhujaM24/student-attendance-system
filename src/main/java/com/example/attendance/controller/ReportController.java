package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceSummaryDto;
import com.example.attendance.dto.DashboardStatsDto;
import com.example.attendance.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public DashboardStatsDto dashboard() {
        return reportService.getDashboardStats();
    }

    @GetMapping("/attendance")
    public List<AttendanceSummaryDto> attendance(
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "search", required = false) String search) {
        return reportService.getAttendanceSummary(subjectId, search);
    }

    @GetMapping("/low-attendance")
    public List<AttendanceSummaryDto> lowAttendance() {
        return reportService.getLowAttendance();
    }
}
