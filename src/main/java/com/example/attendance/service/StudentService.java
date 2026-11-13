package com.example.attendance.service;

import com.example.attendance.dto.StudentRequest;
import com.example.attendance.dto.StudentResponse;
import com.example.attendance.entity.Role;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.exception.ApiException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          AttendanceRepository attendanceRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.default-student-password}") String defaultPassword) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim();
        return studentRepository.search(text).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public StudentResponse getByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student profile not found"));
        return toResponse(student);
    }

    /** Creates the student AND a login account (username = email, password = default password). */
    @Transactional
    public StudentResponse create(StudentRequest request) {
        String code = request.studentCode().trim().toUpperCase();
        String email = request.email().trim().toLowerCase();

        if (studentRepository.existsByStudentCode(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "Student ID '" + code + "' already exists");
        }
        if (studentRepository.existsByEmail(email) || userRepository.existsByUsernameIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email '" + email + "' is already registered");
        }

        User user = new User(email, passwordEncoder.encode(defaultPassword), request.name().trim(), Role.STUDENT);
        Student student = new Student();
        copyFields(student, request, code, email);
        student.setUser(user);   // the user row is saved together with the student

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findOrThrow(id);
        String code = request.studentCode().trim().toUpperCase();
        String email = request.email().trim().toLowerCase();

        if (studentRepository.existsByStudentCodeAndIdNot(code, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Student ID '" + code + "' already exists");
        }
        boolean emailChanged = !email.equals(student.getEmail());
        if (studentRepository.existsByEmailAndIdNot(email, id)
                || (emailChanged && userRepository.existsByUsernameIgnoreCase(email))) {
            throw new ApiException(HttpStatus.CONFLICT, "Email '" + email + "' is already registered");
        }

        copyFields(student, request, code, email);
        student.getUser().setUsername(email);                 // login username follows the email
        student.getUser().setFullName(request.name().trim());
        return toResponse(studentRepository.save(student));
    }

    /** Deletes the student, their attendance records and their login account. */
    @Transactional
    public void delete(Long id) {
        Student student = findOrThrow(id);
        attendanceRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }

    // ---------- helpers ----------

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    private void copyFields(Student student, StudentRequest request, String code, String email) {
        student.setStudentCode(code);
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPhone(request.phone().trim());
        student.setDepartment(request.department().trim());
        student.setYearOfStudy(request.year());
        student.setSection(request.section().trim().toUpperCase());
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getStudentCode(), s.getName(), s.getEmail(), s.getPhone(),
                s.getDepartment(), s.getYearOfStudy(), s.getSection());
    }
}
