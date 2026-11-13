package com.example.attendance.service;

import com.example.attendance.dto.SubjectRequest;
import com.example.attendance.entity.Subject;
import com.example.attendance.exception.ApiException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;

    public SubjectService(SubjectRepository subjectRepository, AttendanceRepository attendanceRepository) {
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public List<Subject> findAll() {
        return subjectRepository.findAllByOrderBySubjectCodeAsc();
    }

    @Transactional
    public Subject create(SubjectRequest request) {
        String code = request.subjectCode().trim().toUpperCase();
        if (subjectRepository.existsBySubjectCode(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "Subject ID '" + code + "' already exists");
        }
        Subject subject = new Subject();
        copyFields(subject, request, code);
        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject update(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));
        String code = request.subjectCode().trim().toUpperCase();
        if (subjectRepository.existsBySubjectCodeAndIdNot(code, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Subject ID '" + code + "' already exists");
        }
        copyFields(subject, request, code);
        return subjectRepository.save(subject);
    }

    /** Deletes the subject together with its attendance records. */
    @Transactional
    public void delete(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));
        attendanceRepository.deleteBySubjectId(id);
        subjectRepository.delete(subject);
    }

    private void copyFields(Subject subject, SubjectRequest request, String code) {
        subject.setSubjectCode(code);
        subject.setName(request.name().trim());
        subject.setDepartment(request.department().trim());
        subject.setSemester(request.semester());
    }
}
