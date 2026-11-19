
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Get Total Students
    @GetMapping("/api/dashboard/total-students")
    public int getTotalStudents() {

        String sql = "SELECT COUNT(*) FROM students";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // 2. Get Average Attendance
    @GetMapping("/api/dashboard/average-attendance")
    public double getAverageAttendance() {

        String sql = """
            SELECT COALESCE(
                ROUND(
                    SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END)
                    * 100.0 / NULLIF(COUNT(*), 0), 2
                ), 0
            )
            FROM attendance
            """;

        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    // 3. Get Average Student Performance
    @GetMapping("/api/dashboard/average-performance")
    public double getAveragePerformance() {

        String sql = """
            SELECT COALESCE(ROUND(AVG(student_percentage), 2), 0)
            FROM (
                SELECT
                    m.student_id,
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0) AS student_percentage
                FROM marks m
                JOIN exams e ON m.exam_id = e.exam_id
                GROUP BY m.student_id
            ) AS performance
            """;

        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    // 4. Get Top 5 Performers
    @GetMapping("/api/dashboard/top-performers")
    public List<Map<String, Object>> getTopPerformers() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                ROUND(
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0), 2
                ) AS percentage
            FROM students s
            JOIN marks m ON s.student_id = m.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            GROUP BY s.student_id, s.roll_no, s.name
            ORDER BY percentage DESC
            LIMIT 5
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 5. Get Low Attendance Alerts
    @GetMapping("/api/dashboard/low-attendance")
    public List<Map<String, Object>> getLowAttendanceAlerts() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                ROUND(
                    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END)
                    * 100.0 / NULLIF(COUNT(a.attendance_id), 0), 2
                ) AS attendance_percentage
            FROM students s
            JOIN attendance a ON s.student_id = a.student_id
            GROUP BY s.student_id, s.roll_no, s.name
            HAVING attendance_percentage < 75
            ORDER BY attendance_percentage ASC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 6. Get Academic Performance Alerts
    @GetMapping("/api/dashboard/academic-alerts")
    public List<Map<String, Object>> getAcademicAlerts() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                ROUND(
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0), 2
                ) AS percentage
            FROM students s
            JOIN marks m ON s.student_id = m.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            GROUP BY s.student_id, s.roll_no, s.name
            HAVING percentage < 40
            ORDER BY percentage ASC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 7. Get Total Attendance Records
    @GetMapping("/api/dashboard/total-attendance-records")
    public int getTotalAttendanceRecords() {

        String sql = "SELECT COUNT(*) FROM attendance";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
