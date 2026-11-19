
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;


@RestController
public class StudentController {

    private final JdbcTemplate jdbcTemplate;

    public StudentController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Common validation method
    private String validateStudent(Map<String, Object> student) {

        String rollNo = getValue(student, "roll_no");
        String name = getValue(student, "name");
        String email = getValue(student, "email");
        String department = getValue(student, "department");
        String year = getValue(student, "year_of_study");
        String phone = getValue(student, "phone");

        if (rollNo.isEmpty()) {
            return "Roll number is required";
        }

        if (name.isEmpty() || !name.matches("[a-zA-Z .'-]+")) {
            return "Enter a valid student name";
        }

        if (email.isEmpty() ||
            !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Enter a valid email address";
        }

        if (department.isEmpty()) {
            return "Department is required";
        }

        try {
            int studyYear = Integer.parseInt(year);

            if (studyYear < 1 || studyYear > 6) {
                return "Year of study must be between 1 and 6";
            }

        } catch (NumberFormatException e) {
            return "Year of study must be a valid number";
        }

        if (!phone.matches("[0-9]{10}")) {
            return "Phone number must contain exactly 10 digits";
        }

        return null;
    }

    private String getValue(Map<String, Object> student, String key) {
        Object value = student.get(key);
        return value == null ? "" : value.toString().trim();
    }

    // 1. Get All Students
    @GetMapping("/api/students")
    public List<Map<String, Object>> getAllStudents() {

        String sql = """
            SELECT student_id, roll_no, name, email,
                   department, year_of_study, phone
            FROM students
            ORDER BY student_id
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 2. Add New Student
    @PostMapping("/api/students")
    public ResponseEntity<Map<String, String>> addStudent(
            @RequestBody Map<String, Object> student) {

        String validationError = validateStudent(student);

        if (validationError != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", validationError));
        }

        String sql = """
            INSERT INTO students
            (roll_no, name, email, department, year_of_study, phone)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try {

            jdbcTemplate.update(
                sql,
                getValue(student, "roll_no"),
                getValue(student, "name"),
                getValue(student, "email"),
                getValue(student, "department"),
                Integer.parseInt(getValue(student, "year_of_study")),
                getValue(student, "phone")
            );

            return ResponseEntity.ok(
                Map.of("message", "Student added successfully!")
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                        "Roll number or email already exists."));
        }
    }

    // 3. Update Student
    @PutMapping("/api/students/{rollNo}")
    public ResponseEntity<Map<String, String>> updateStudent(
            @PathVariable String rollNo,
            @RequestBody Map<String, Object> student) {

        String validationError = validateStudent(student);

        if (validationError != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", validationError));
        }

        String sql = """
            UPDATE students
            SET name = ?,
                email = ?,
                department = ?,
                year_of_study = ?,
                phone = ?
            WHERE roll_no = ?
            """;

        try {

            int rows = jdbcTemplate.update(
                sql,
                getValue(student, "name"),
                getValue(student, "email"),
                getValue(student, "department"),
                Integer.parseInt(getValue(student, "year_of_study")),
                getValue(student, "phone"),
                rollNo
            );

            if (rows == 0) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Student not found!"));
            }

            return ResponseEntity.ok(
                Map.of("message", "Student updated successfully!")
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                        "Email already belongs to another student."));
        }
    }

    // 4. Delete Student
    @DeleteMapping("/api/students/{rollNo}")
    public ResponseEntity<Map<String, String>> deleteStudent(
            @PathVariable String rollNo) {

        String sql = """
            DELETE FROM students
            WHERE roll_no = ?
            """;

        try {

            int rows = jdbcTemplate.update(sql, rollNo);

            if (rows == 0) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Student not found!"));
            }

            return ResponseEntity.ok(
                Map.of("message", "Student deleted successfully!")
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                        "Cannot delete student because related attendance, marks or enrollment records exist."));
        }
    }
}
