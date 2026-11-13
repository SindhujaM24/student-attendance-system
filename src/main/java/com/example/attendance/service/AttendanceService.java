package com.example.attendance.service;

import com.example.attendance.dto.AttendanceSaveRequest;
import com.example.attendance.dto.AttendanceSheetRow;
import com.example.attendance.dto.MessageResponse;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.AttendanceStatus;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.Subject;
import com.example.attendance.exception.ApiException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Builds the attendance sheet: students of the subject's department in the chosen year + section.
     * If attendance was already saved for that date, the saved status is returned so it can be edited.
     */
    @Transactional(readOnly = true)
    public List<AttendanceSheetRow> getSheet(Long subjectId, LocalDate date, Integer year, String section) {
        Subject subject = findSubject(subjectId);

        List<Student> students = studentRepository
                .findByDepartmentAndYearOfStudyAndSectionOrderByStudentCodeAsc(
                        subject.getDepartment(), year, section.trim().toUpperCase());

        Map<Long, AttendanceStatus> savedStatus = new HashMap<>();
        for (Attendance record : attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date)) {
            savedStatus.put(record.getStudent().getId(), record.getStatus());
        }

        return students.stream()
                .map(s -> new AttendanceSheetRow(s.getId(), s.getStudentCode(), s.getName(), savedStatus.get(s.getId())))
                .toList();
    }

    /** Saves attendance. If a record already exists for student + subject + date, it is updated (edit). */
    @Transactional
    public MessageResponse save(AttendanceSaveRequest request) {
        if (request.date().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Attendance cannot be marked for a future date");
        }
        Subject subject = findSubject(request.subjectId());

        Map<Long, Attendance> existing = new HashMap<>();
        for (Attendance record : attendanceRepository.findBySubjectIdAndAttendanceDate(subject.getId(), request.date())) {
            existing.put(record.getStudent().getId(), record);
        }

        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceSaveRequest.Item item : request.records()) {
            Attendance record = existing.get(item.studentId());
            if (record == null) {
                Student student = studentRepository.findById(item.studentId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
                record = new Attendance();
                record.setStudent(student);
                record.setSubject(subject);
                record.setAttendanceDate(request.date());
            }
            record.setStatus(item.status());
            toSave.add(record);
        }
        attendanceRepository.saveAll(toSave);
        return new MessageResponse("Attendance saved for " + toSave.size() + " student(s)");
    }

    private Subject findSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));
    }
}
