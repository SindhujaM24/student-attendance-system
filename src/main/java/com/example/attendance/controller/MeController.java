package com.example.attendance.controller;

import com.example.attendance.config.AuthInterceptor;
import com.example.attendance.dto.StudentAttendanceDto;
import com.example.attendance.dto.StudentResponse;
import com.example.attendance.service.ReportService;
import com.example.attendance.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** APIs used by a logged-in student to see only their own data. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final StudentService studentService;
    private final ReportService reportService;

    public MeController(StudentService studentService, ReportService reportService) {
        this.studentService = studentService;
        this.reportService = reportService;
    }

    @GetMapping("/profile")
    public StudentResponse profile(HttpSession session) {
        return studentService.getByUserId(currentUserId(session));
    }

    @GetMapping("/attendance")
    public StudentAttendanceDto attendance(HttpSession session) {
        StudentResponse student = studentService.getByUserId(currentUserId(session));
        return reportService.getStudentAttendance(student.id());
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(AuthInterceptor.SESSION_USER_ID);
    }
}
