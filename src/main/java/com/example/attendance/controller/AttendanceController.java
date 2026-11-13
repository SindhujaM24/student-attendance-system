package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceSaveRequest;
import com.example.attendance.dto.AttendanceSheetRow;
import com.example.attendance.dto.MessageResponse;
import com.example.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** GET /api/attendance/sheet?subjectId=1&date=2026-10-05&year=3&section=A */
    @GetMapping("/sheet")
    public List<AttendanceSheetRow> sheet(
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("year") Integer year,
            @RequestParam("section") String section) {
        return attendanceService.getSheet(subjectId, date, year, section);
    }

    /** Save new attendance, or edit attendance that was saved earlier. */
    @PostMapping
    public MessageResponse save(@Valid @RequestBody AttendanceSaveRequest request) {
        return attendanceService.save(request);
    }
}
