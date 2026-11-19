
package com.college.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentPortalController {

    private final JdbcTemplate jdbcTemplate;

    public StudentPortalController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Get logged-in student's profile
    @GetMapping("/me")
    public Map<String, Object> getMyProfile(
            Authentication authentication) {

        String sql = """
            SELECT s.student_id, s.roll_no, s.name,
                   s.email, s.department, s.year_of_study
            FROM users u
            JOIN students s ON u.student_id = s.student_id
            WHERE u.username = ?
              AND u.role = 'STUDENT'
            """;

        return jdbcTemplate.queryForMap(
                sql, authentication.getName());
    }

    // 2. Get logged-in student's attendance summary
    @GetMapping("/attendance")
    public List<Map<String, Object>> getMyAttendance(
            Authentication authentication) {

        String sql = """
            SELECT sub.subject_code,
                   sub.subject_name,
                   COUNT(a.attendance_id) AS total_classes,
                   SUM(CASE WHEN a.status = 'PRESENT'
                       THEN 1 ELSE 0 END) AS present_classes,
                   ROUND(
                       SUM(CASE WHEN a.status = 'PRESENT'
                           THEN 1 ELSE 0 END) * 100.0 /
                       NULLIF(COUNT(a.attendance_id), 0), 2
                   ) AS attendance_percentage
            FROM users u
            JOIN enrollments en ON u.student_id = en.student_id
            JOIN subjects sub ON en.subject_id = sub.subject_id
            LEFT JOIN attendance a
                ON a.student_id = en.student_id
                AND a.subject_id = en.subject_id
            WHERE u.username = ?
              AND u.role = 'STUDENT'
            GROUP BY sub.subject_id,
                     sub.subject_code,
                     sub.subject_name
            ORDER BY sub.subject_name
            """;

        return jdbcTemplate.queryForList(
                sql, authentication.getName());
    }

    // 3. Get logged-in student's exam-wise marks
    @GetMapping("/marks")
    public List<Map<String, Object>> getMyMarks(
            Authentication authentication) {

        String sql = """
            SELECT
                e.exam_id,
                sub.subject_code,
                sub.subject_name,
                e.exam_name,
                e.max_marks,
                m.obtained_marks,
                ROUND(
                    m.obtained_marks * 100.0 /
                    NULLIF(e.max_marks, 0), 2
                ) AS percentage
            FROM users u
            JOIN students s
                ON u.student_id = s.student_id
            JOIN marks m
                ON s.student_id = m.student_id
            JOIN exams e
                ON m.exam_id = e.exam_id
            JOIN subjects sub
                ON e.subject_id = sub.subject_id
            WHERE u.username = ?
              AND u.role = 'STUDENT'
            ORDER BY sub.subject_name, e.exam_name
            """;

        return jdbcTemplate.queryForList(
                sql, authentication.getName());
    }

    // 4. Get logged-in student's overall academic performance
    @GetMapping("/performance")
    public Map<String, Object> getMyPerformance(
            Authentication authentication) {

        String sql = """
            SELECT
                COUNT(m.mark_id) AS total_exams,
                COALESCE(SUM(m.obtained_marks), 0)
                    AS total_obtained_marks,
                COALESCE(SUM(e.max_marks), 0)
                    AS total_max_marks,
                COALESCE(
                    ROUND(
                        SUM(m.obtained_marks) * 100.0 /
                        NULLIF(SUM(e.max_marks), 0), 2
                    ), 0
                ) AS overall_percentage
            FROM users u
            JOIN students s
                ON u.student_id = s.student_id
            LEFT JOIN marks m
                ON s.student_id = m.student_id
            LEFT JOIN exams e
                ON m.exam_id = e.exam_id
            WHERE u.username = ?
              AND u.role = 'STUDENT'
            """;

        return jdbcTemplate.queryForMap(
                sql, authentication.getName());
    }
}
