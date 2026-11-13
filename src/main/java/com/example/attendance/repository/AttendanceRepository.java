package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Already saved attendance of one subject on one date (so it can be edited). */
    List<Attendance> findBySubjectIdAndAttendanceDate(Long subjectId, LocalDate attendanceDate);

    List<Attendance> findTop10ByStudentIdOrderByAttendanceDateDescIdDesc(Long studentId);

    long countByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

    void deleteByStudentId(Long studentId);

    void deleteBySubjectId(Long subjectId);

    /**
     * One row per student + subject:
     * [studentDbId, studentCode, studentName, subjectDbId, subjectName, totalClasses, presentCount]
     */
    @Query("""
            select st.id, st.studentCode, st.name, sb.id, sb.name,
                   count(a.id),
                   sum(case when a.status = com.example.attendance.entity.AttendanceStatus.PRESENT then 1 else 0 end)
            from Attendance a
            join a.student st
            join a.subject sb
            group by st.id, st.studentCode, st.name, sb.id, sb.name
            order by st.name, sb.name
            """)
    List<Object[]> getSummaryForAllStudents();

    /** Same as above but only for one student (student dashboard). */
    @Query("""
            select st.id, st.studentCode, st.name, sb.id, sb.name,
                   count(a.id),
                   sum(case when a.status = com.example.attendance.entity.AttendanceStatus.PRESENT then 1 else 0 end)
            from Attendance a
            join a.student st
            join a.subject sb
            where st.id = :studentId
            group by st.id, st.studentCode, st.name, sb.id, sb.name
            order by sb.name
            """)
    List<Object[]> getSummaryForStudent(@Param("studentId") Long studentId);
}
