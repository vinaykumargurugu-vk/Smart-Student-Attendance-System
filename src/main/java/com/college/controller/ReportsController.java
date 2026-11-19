
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final JdbcTemplate jdbcTemplate;

    public ReportsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Student-wise Academic Report
    @GetMapping("/student/{rollNo}")
    public List<Map<String, Object>> getStudentReport(
            @PathVariable String rollNo) {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                s.department,
                COALESCE(a.total_classes, 0) AS total_classes,
                COALESCE(a.present_classes, 0) AS present_classes,
                COALESCE(a.attendance_percentage, 0)
                    AS attendance_percentage,
                COALESCE(m.total_obtained, 0) AS total_obtained,
                COALESCE(m.total_maximum, 0) AS total_maximum,
                COALESCE(m.marks_percentage, 0) AS marks_percentage
            FROM students s

            LEFT JOIN (
                SELECT
                    student_id,
                    COUNT(*) AS total_classes,
                    SUM(CASE WHEN status = 'PRESENT'
                        THEN 1 ELSE 0 END) AS present_classes,
                    ROUND(
                        SUM(CASE WHEN status = 'PRESENT'
                            THEN 1 ELSE 0 END) * 100.0 /
                        NULLIF(COUNT(*), 0), 2
                    ) AS attendance_percentage
                FROM attendance
                GROUP BY student_id
            ) a ON s.student_id = a.student_id

            LEFT JOIN (
                SELECT
                    m.student_id,
                    SUM(m.obtained_marks) AS total_obtained,
                    SUM(e.max_marks) AS total_maximum,
                    ROUND(
                        SUM(m.obtained_marks) * 100.0 /
                        NULLIF(SUM(e.max_marks), 0), 2
                    ) AS marks_percentage
                FROM marks m
                JOIN exams e ON m.exam_id = e.exam_id
                GROUP BY m.student_id
            ) m ON s.student_id = m.student_id

            WHERE s.roll_no = ?
            """;

        return jdbcTemplate.queryForList(sql, rollNo);
    }


    // 2. Attendance Summary
    @GetMapping("/attendance-summary")
    public List<Map<String, Object>> getAttendanceSummary() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_name,
                COUNT(a.attendance_id) AS total_classes,
                SUM(CASE WHEN a.status = 'PRESENT'
                    THEN 1 ELSE 0 END) AS present_classes,
                ROUND(
                    SUM(CASE WHEN a.status = 'PRESENT'
                        THEN 1 ELSE 0 END) * 100.0 /
                    NULLIF(COUNT(a.attendance_id), 0), 2
                ) AS attendance_percentage
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN subjects sub ON a.subject_id = sub.subject_id
            GROUP BY s.student_id, s.roll_no, s.name,
                     sub.subject_id, sub.subject_name
            ORDER BY s.roll_no
            """;

        return jdbcTemplate.queryForList(sql);
    }


    // 3. Marks Summary
    @GetMapping("/marks-summary")
    public List<Map<String, Object>> getMarksSummary() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                SUM(m.obtained_marks) AS total_obtained,
                SUM(e.max_marks) AS total_maximum,
                ROUND(
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0), 2
                ) AS percentage
            FROM students s
            JOIN marks m ON s.student_id = m.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            GROUP BY s.student_id, s.roll_no, s.name
            ORDER BY percentage DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }


    // 4. Subject-wise Summary
    @GetMapping("/subject-summary")
    public List<Map<String, Object>> getSubjectSummary() {

        String sql = """
            SELECT
                sub.subject_code,
                sub.subject_name,
                COUNT(m.mark_id) AS total_marks_records,
                ROUND(AVG(
                    m.obtained_marks * 100.0 / e.max_marks
                ), 2) AS average_percentage
            FROM subjects sub
            JOIN exams e ON sub.subject_id = e.subject_id
            LEFT JOIN marks m ON e.exam_id = m.exam_id
            GROUP BY sub.subject_id, sub.subject_code,
                     sub.subject_name
            ORDER BY average_percentage DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }
}
