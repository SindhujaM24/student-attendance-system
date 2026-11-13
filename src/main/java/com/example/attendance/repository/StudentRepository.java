package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByEmail(String email);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Finds the student that owns a login account (user.id). */
    Optional<Student> findByUserId(Long userId);

    /** Students of one class - used to build the attendance sheet. */
    List<Student> findByDepartmentAndYearOfStudyAndSectionOrderByStudentCodeAsc(
            String department, Integer yearOfStudy, String section);

    /** Search box on the Students page. An empty keyword returns everyone. */
    @Query("""
            select s from Student s
            where lower(s.name) like lower(concat('%', :keyword, '%'))
               or lower(s.studentCode) like lower(concat('%', :keyword, '%'))
               or lower(s.email) like lower(concat('%', :keyword, '%'))
               or lower(s.department) like lower(concat('%', :keyword, '%'))
            order by s.studentCode
            """)
    List<Student> search(@Param("keyword") String keyword);
}
