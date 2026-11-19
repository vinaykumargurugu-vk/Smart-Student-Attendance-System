
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.Date;

@RestController
public class AttendanceController {

    private final JdbcTemplate jdbcTemplate;

    public AttendanceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Get All Attendance Records
    @GetMapping("/api/attendance")
    public List<Map<String, Object>> getAllAttendance() {

        String sql = """
            SELECT
                a.attendance_id,
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                a.attendance_date,
                a.status
            FROM attendance a
            JOIN students s
                ON a.student_id = s.student_id
            JOIN subjects sub
                ON a.subject_id = sub.subject_id
            ORDER BY a.attendance_date DESC, s.roll_no
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 2. Mark Student Attendance
    @PostMapping("/api/attendance")
    public ResponseEntity<Map<String, String>> markAttendance(
            @RequestBody Map<String, String> attendance) {

        // Get input values
        String rollNo = attendance.get("roll_no");
        String subjectCode = attendance.get("subject_code");
        String attendanceDate = attendance.get("attendance_date");
        String status = attendance.get("status");

        // Validate required fields
        if (rollNo == null || rollNo.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Roll number is required."));
        }

        if (subjectCode == null || subjectCode.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Subject code is required."));
        }

        if (attendanceDate == null || attendanceDate.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Attendance date is required."));
        }

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Attendance status is required."));
        }

        // Validate attendance status
        status = status.trim().toUpperCase();

        if (!status.equals("PRESENT") && !status.equals("ABSENT")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error",
                    "Invalid status! Enter PRESENT or ABSENT."));
        }

        // Validate date format
        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(attendanceDate.trim());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error",
                    "Invalid date! Use YYYY-MM-DD format."));
        }

        String sql = """
            INSERT INTO attendance
            (student_id, subject_id, attendance_date, status)
            SELECT s.student_id, sub.subject_id, ?, ?
            FROM students s
            JOIN enrollments en
                ON s.student_id = en.student_id
            JOIN subjects sub
                ON en.subject_id = sub.subject_id
            WHERE s.roll_no = ?
              AND sub.subject_code = ?
            """;

        try {

            int rows = jdbcTemplate.update(
                sql,
                Date.valueOf(parsedDate),
                status,
                rollNo.trim(),
                subjectCode.trim()
            );

            if (rows == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error",
                        "Student or subject not found, or student is not enrolled in this subject."));
            }

            return ResponseEntity.ok(
                Map.of("message", "Attendance marked successfully!")
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error",
                    "Attendance already marked for this student, subject and date."));
        }
    }

    // 3. Get Attendance Percentage
    @GetMapping("/api/attendance/percentage")
    public List<Map<String, Object>> getAttendancePercentage() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                COUNT(a.attendance_id) AS total_classes,
                SUM(CASE
                    WHEN a.status = 'PRESENT' THEN 1
                    ELSE 0
                END) AS present_classes,
                ROUND(
                    SUM(CASE
                        WHEN a.status = 'PRESENT' THEN 1
                        ELSE 0
                    END) * 100.0 / COUNT(a.attendance_id),
                    2
                ) AS attendance_percentage
            FROM attendance a
            JOIN students s
                ON a.student_id = s.student_id
            JOIN subjects sub
                ON a.subject_id = sub.subject_id
            GROUP BY
                s.student_id,
                s.roll_no,
                s.name,
                sub.subject_id,
                sub.subject_code,
                sub.subject_name
            ORDER BY s.roll_no, sub.subject_code
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 4. Get Low Attendance Alerts
    @GetMapping("/api/attendance/low-attendance")
    public List<Map<String, Object>> getLowAttendanceAlerts() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                COUNT(a.attendance_id) AS total_classes,
                SUM(CASE
                    WHEN a.status = 'PRESENT' THEN 1
                    ELSE 0
                END) AS present_classes,
                ROUND(
                    SUM(CASE
                        WHEN a.status = 'PRESENT' THEN 1
                        ELSE 0
                    END) * 100.0 / COUNT(a.attendance_id),
                    2
                ) AS attendance_percentage
            FROM attendance a
            JOIN students s
                ON a.student_id = s.student_id
            JOIN subjects sub
                ON a.subject_id = sub.subject_id
            GROUP BY
                s.student_id,
                s.roll_no,
                s.name,
                sub.subject_id,
                sub.subject_code,
                sub.subject_name
            HAVING attendance_percentage < 75
            ORDER BY attendance_percentage ASC
            """;

        return jdbcTemplate.queryForList(sql);
    }
}
