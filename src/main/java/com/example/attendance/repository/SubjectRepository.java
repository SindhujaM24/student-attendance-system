package com.example.attendance.repository;

import com.example.attendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsBySubjectCode(String subjectCode);

    boolean existsBySubjectCodeAndIdNot(String subjectCode, Long id);

    List<Subject> findAllByOrderBySubjectCodeAsc();
}
