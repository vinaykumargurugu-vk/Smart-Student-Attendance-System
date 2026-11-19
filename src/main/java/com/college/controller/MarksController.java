
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final JdbcTemplate jdbcTemplate;

    public MarksController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Helper method for JSON responses
    private ResponseEntity<Map<String, String>> response(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(Map.of(
                    status.is2xxSuccessful() ? "message" : "error",
                    message
                ));
    }

    // 1. Get All Student Marks
    @GetMapping
    public List<Map<String, Object>> getAllMarks() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                e.exam_id,
                e.exam_name,
                e.max_marks,
                m.obtained_marks,
                ROUND(
                    m.obtained_marks * 100.0 / e.max_marks, 2
                ) AS percentage
            FROM marks m
            JOIN students s ON m.student_id = s.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            ORDER BY s.roll_no, e.exam_id
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 2. Add Student Marks
    @PostMapping
    public ResponseEntity<Map<String, String>> addMarks(
            @RequestBody Map<String, Object> marks) {

        try {

            if (marks.get("roll_no") == null ||
                marks.get("exam_id") == null ||
                marks.get("obtained_marks") == null) {

                return response(
                    HttpStatus.BAD_REQUEST,
                    "Please provide roll_no, exam_id and obtained_marks!"
                );
            }

            String rollNo = marks.get("roll_no").toString().trim();

            int examId = Integer.parseInt(
                marks.get("exam_id").toString()
            );

            double obtainedMarks = Double.parseDouble(
                marks.get("obtained_marks").toString()
            );

            if (rollNo.isEmpty() || examId <= 0 ||
                !Double.isFinite(obtainedMarks)) {

                return response(
                    HttpStatus.BAD_REQUEST,
                    "Please provide valid input values!"
                );
            }

            // Check student enrollment and exam
            String checkSql = """
                SELECT s.student_id, e.max_marks
                FROM students s
                JOIN enrollments en
                    ON s.student_id = en.student_id
                JOIN exams e
                    ON en.subject_id = e.subject_id
                WHERE s.roll_no = ?
                  AND e.exam_id = ?
                """;

            List<Map<String, Object>> result =
                jdbcTemplate.queryForList(checkSql, rollNo, examId);

            if (result.isEmpty()) {
                return response(
                    HttpStatus.NOT_FOUND,
                    "Student, exam or enrollment not found!"
                );
            }

            int studentId = ((Number)
                result.get(0).get("student_id")).intValue();

            double maxMarks = ((Number)
                result.get(0).get("max_marks")).doubleValue();

            // Validate marks
            if (obtainedMarks < 0 || obtainedMarks > maxMarks) {
                return response(
                    HttpStatus.BAD_REQUEST,
                    "Enter marks between 0 and " + maxMarks
                );
            }

            // Insert marks
            String insertSql = """
                INSERT INTO marks
                (student_id, exam_id, obtained_marks)
                VALUES (?, ?, ?)
                """;

            jdbcTemplate.update(
                insertSql,
                studentId,
                examId,
                obtainedMarks
            );

            return response(
                HttpStatus.OK,
                "Marks added successfully!"
            );

        } catch (DataIntegrityViolationException e) {

            return response(
                HttpStatus.CONFLICT,
                "Marks already exist for this student and exam!"
            );

        } catch (NumberFormatException e) {

            return response(
                HttpStatus.BAD_REQUEST,
                "Invalid exam ID or marks!"
            );
        }
    }

    // 3. Get Marks of a Particular Student
    @GetMapping("/student/{rollNo}")
    public List<Map<String, Object>> getStudentMarks(
            @PathVariable String rollNo) {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                e.exam_name,
                e.max_marks,
                m.obtained_marks,
                ROUND(
                    m.obtained_marks * 100.0 / e.max_marks, 2
                ) AS percentage
            FROM marks m
            JOIN students s ON m.student_id = s.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            WHERE s.roll_no = ?
            ORDER BY e.exam_id
            """;

        return jdbcTemplate.queryForList(sql, rollNo);
    }
}
