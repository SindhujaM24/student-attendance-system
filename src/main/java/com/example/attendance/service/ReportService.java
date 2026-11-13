package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRecordDto;
import com.example.attendance.dto.AttendanceSummaryDto;
import com.example.attendance.dto.DashboardStatsDto;
import com.example.attendance.dto.StudentAttendanceDto;
import com.example.attendance.entity.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** All attendance percentage calculations happen here. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final double minimumPercentage;

    public ReportService(AttendanceRepository attendanceRepository,
                         StudentRepository studentRepository,
                         SubjectRepository subjectRepository,
                         @Value("${app.attendance.minimum-percentage}") double minimumPercentage) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.minimumPercentage = minimumPercentage;
    }

    public DashboardStatsDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        long markedToday = attendanceRepository.countByAttendanceDate(today);
        long presentToday = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT);
        long studentsBelow = getLowAttendance().stream().map(AttendanceSummaryDto::studentId).distinct().count();

        return new DashboardStatsDto(studentRepository.count(), subjectRepository.count(),
                markedToday, presentToday, studentsBelow, minimumPercentage);
    }

    /** Report page: optional filter by subject and by student name / Student ID. */
    public List<AttendanceSummaryDto> getAttendanceSummary(Long subjectId, String search) {
        String keyword = (search == null) ? "" : search.trim().toLowerCase();

        return attendanceRepository.getSummaryForAllStudents().stream()
                .map(this::toSummary)
                .filter(row -> subjectId == null || row.subjectId().equals(subjectId))
                .filter(row -> keyword.isEmpty()
                        || row.studentName().toLowerCase().contains(keyword)
                        || row.studentCode().toLowerCase().contains(keyword))
                .toList();
    }

    /** Student + subject combinations below the minimum percentage, lowest first. */
    public List<AttendanceSummaryDto> getLowAttendance() {
        return attendanceRepository.getSummaryForAllStudents().stream()
                .map(this::toSummary)
                .filter(AttendanceSummaryDto::low)
                .sorted(Comparator.comparingDouble(AttendanceSummaryDto::percentage))
                .toList();
    }

    /** Data for the student dashboard. */
    public StudentAttendanceDto getStudentAttendance(Long studentDbId) {
        List<AttendanceSummaryDto> subjects = attendanceRepository.getSummaryForStudent(studentDbId).stream()
                .map(this::toSummary)
                .toList();

        long total = subjects.stream().mapToLong(AttendanceSummaryDto::totalClasses).sum();
        long present = subjects.stream().mapToLong(AttendanceSummaryDto::present).sum();

        List<AttendanceRecordDto> recent = attendanceRepository
                .findTop10ByStudentIdOrderByAttendanceDateDescIdDesc(studentDbId).stream()
                .map(a -> new AttendanceRecordDto(a.getAttendanceDate(), a.getSubject().getName(), a.getStatus()))
                .toList();

        return new StudentAttendanceDto(calculatePercentage(present, total), total, present, total - present,
                minimumPercentage, subjects, recent);
    }

    /** Attendance % = present / total classes * 100, rounded to 1 decimal (35/40 -> 87.5). */
    public static double calculatePercentage(long present, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(present * 1000.0 / total) / 10.0;
    }

    private AttendanceSummaryDto toSummary(Object[] row) {
        long total = ((Number) row[5]).longValue();
        long present = (row[6] == null) ? 0 : ((Number) row[6]).longValue();
        boolean low = total > 0 && (present * 100.0 / total) < minimumPercentage;

        return new AttendanceSummaryDto((Long) row[0], (String) row[1], (String) row[2],
                (Long) row[3], (String) row[4],
                total, present, total - present, calculatePercentage(present, total), low);
    }
}
