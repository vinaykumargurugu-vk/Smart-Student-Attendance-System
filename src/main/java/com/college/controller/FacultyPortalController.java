
package com.college.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/faculty")
public class FacultyPortalController {

    private final JdbcTemplate jdbcTemplate;

    public FacultyPortalController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Get logged-in faculty profile
    @GetMapping("/me")
    public Map<String, Object> getMyProfile(
            Authentication authentication) {

        String sql = """
            SELECT f.faculty_id, f.name, f.email,
                   f.department, f.phone
            FROM users u
            JOIN faculty f ON u.faculty_id = f.faculty_id
            WHERE u.username = ?
              AND u.role = 'FACULTY'
            """;

        return jdbcTemplate.queryForMap(
                sql, authentication.getName());
    }

    // 2. Get subjects assigned to logged-in faculty
    @GetMapping("/subjects")
    public List<Map<String, Object>> getMySubjects(
            Authentication authentication) {

        String sql = """
            SELECT s.subject_id, s.subject_code, s.subject_name
            FROM users u
            JOIN faculty f ON u.faculty_id = f.faculty_id
            JOIN subjects s ON s.faculty_id = f.faculty_id
            WHERE u.username = ?
              AND u.role = 'FACULTY'
            ORDER BY s.subject_name
            """;

        return jdbcTemplate.queryForList(
                sql, authentication.getName());
    }

    // 3. Get students enrolled in faculty's subject
    @GetMapping("/students")
    public List<Map<String, Object>> getStudentsBySubject(
            @RequestParam("subjectId") int subjectId,
            Authentication authentication) {

        verifyFacultySubject(
                authentication.getName(), subjectId);

        String sql = """
            SELECT st.student_id, st.roll_no, st.name,
                   st.email, st.department, st.year_of_study
            FROM enrollments e
            JOIN students st ON st.student_id = e.student_id
            WHERE e.subject_id = ?
            ORDER BY st.roll_no
            """;

        return jdbcTemplate.queryForList(sql, subjectId);
    }

    // 4. Mark student attendance
    @PostMapping("/attendance")
    public Map<String, Object> markAttendance(
            @RequestBody AttendanceRequest request,
            Authentication authentication) {

        if (request.status() == null ||
                (!request.status().equals("PRESENT") &&
                 !request.status().equals("ABSENT"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PRESENT or ABSENT");
        }

        if (request.attendanceDate() == null ||
                request.attendanceDate().isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid attendance date");
        }

        if (request.studentId() <= 0 ||
                request.subjectId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid student or subject ID");
        }

        verifyFacultySubject(
                authentication.getName(),
                request.subjectId());

        String enrollmentSql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
              AND subject_id = ?
            """;

        Integer enrollmentCount = jdbcTemplate.queryForObject(
                enrollmentSql,
                Integer.class,
                request.studentId(),
                request.subjectId());

        if (enrollmentCount == null || enrollmentCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student is not enrolled in this subject");
        }

        String insertSql = """
            INSERT INTO attendance
                (student_id, subject_id, attendance_date, status)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                status = VALUES(status)
            """;

        jdbcTemplate.update(
                insertSql,
                request.studentId(),
                request.subjectId(),
                request.attendanceDate(),
                request.status());

        return Map.of(
                "message", "Attendance saved successfully",
                "studentId", request.studentId(),
                "subjectId", request.subjectId(),
                "date", request.attendanceDate().toString(),
                "status", request.status());
    }

    // 5. Attendance request record
    public record AttendanceRequest(
            int studentId,
            int subjectId,
            LocalDate attendanceDate,
            String status) {
    }

    // 6. Common faculty subject authorization
    private void verifyFacultySubject(
            String username, int subjectId) {

        if (subjectId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid subject ID");
        }

        String sql = """
            SELECT COUNT(*)
            FROM users u
            JOIN subjects s ON s.faculty_id = u.faculty_id
            WHERE u.username = ?
              AND u.role = 'FACULTY'
              AND s.subject_id = ?
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, username, subjectId);

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not assigned to this subject");
        }
    }

    // 7. View attendance history by subject and date
    @GetMapping("/attendance/history")
    public List<Map<String, Object>> getAttendanceHistory(
            @RequestParam("subjectId") int subjectId,
            @RequestParam("attendanceDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate attendanceDate,
            Authentication authentication) {

        verifyFacultySubject(
                authentication.getName(), subjectId);

        String historySql = """
            SELECT
                st.student_id,
                st.roll_no,
                st.name,
                sub.subject_code,
                sub.subject_name,
                a.attendance_date,
                a.status
            FROM attendance a
            JOIN students st
                ON a.student_id = st.student_id
            JOIN subjects sub
                ON a.subject_id = sub.subject_id
            WHERE a.subject_id = ?
              AND a.attendance_date = ?
            ORDER BY st.roll_no
            """;

        return jdbcTemplate.queryForList(
                historySql, subjectId, attendanceDate);
    }

    // 8. Attendance summary for today's classes
    @GetMapping("/attendance/summary")
    public Map<String, Object> getAttendanceSummary(
            @RequestParam("subjectId") int subjectId,
            Authentication authentication) {

        verifyFacultySubject(
                authentication.getName(), subjectId);

        String summarySql = """
            SELECT
                COUNT(DISTINCT e.student_id) AS total_students,

                COUNT(DISTINCT CASE
                    WHEN a.status = 'PRESENT'
                    THEN e.student_id
                END) AS present_students,

                COUNT(DISTINCT CASE
                    WHEN a.status = 'ABSENT'
                    THEN e.student_id
                END) AS absent_students

            FROM enrollments e

            LEFT JOIN attendance a
                ON e.student_id = a.student_id
                AND e.subject_id = a.subject_id
                AND a.attendance_date = CURDATE()

            WHERE e.subject_id = ?
            """;

        Map<String, Object> summary =
                jdbcTemplate.queryForMap(summarySql, subjectId);

        int total = ((Number)
                summary.get("total_students")).intValue();

        int present = ((Number)
                summary.get("present_students")).intValue();

        double percentage = total == 0
                ? 0
                : (present * 100.0) / total;

        summary.put(
                "attendance_percentage",
                Math.round(percentage * 100.0) / 100.0);

        return summary;
    }

    // 9. Get students whose attendance is below 75%
    @GetMapping("/attendance/low-attendance")
    public List<Map<String, Object>> getLowAttendanceStudents(
            @RequestParam("subjectId") int subjectId,
            Authentication authentication) {

        verifyFacultySubject(
                authentication.getName(), subjectId);

        String sql = """
            SELECT
                st.student_id,
                st.roll_no,
                st.name,

                COUNT(a.attendance_id) AS total_classes,

                SUM(
                    CASE
                        WHEN a.status = 'PRESENT' THEN 1
                        ELSE 0
                    END
                ) AS classes_attended,

                SUM(
                    CASE
                        WHEN a.status = 'ABSENT' THEN 1
                        ELSE 0
                    END
                ) AS classes_missed,

                ROUND(
                    SUM(
                        CASE
                            WHEN a.status = 'PRESENT' THEN 1
                            ELSE 0
                        END
                    ) * 100.0 /
                    NULLIF(COUNT(a.attendance_id), 0),
                    2
                ) AS attendance_percentage

            FROM enrollments e

            JOIN students st
                ON e.student_id = st.student_id

            LEFT JOIN attendance a
                ON e.student_id = a.student_id
                AND e.subject_id = a.subject_id

            WHERE e.subject_id = ?

            GROUP BY
                st.student_id,
                st.roll_no,
                st.name

            HAVING COUNT(a.attendance_id) > 0
               AND attendance_percentage < 75

            ORDER BY attendance_percentage ASC
            """;

        return jdbcTemplate.queryForList(sql, subjectId);
    }

    // 10. Get exams assigned to faculty's subject
    @GetMapping("/exams")
    public List<Map<String, Object>> getExamsBySubject(
            @RequestParam("subjectId") int subjectId,
            Authentication authentication) {

        verifyFacultySubject(
                authentication.getName(), subjectId);

        String sql = """
            SELECT exam_id, subject_id, exam_name, max_marks
            FROM exams
            WHERE subject_id = ?
            ORDER BY exam_id
            """;

        return jdbcTemplate.queryForList(sql, subjectId);
    }

    // 11. Verify that an exam belongs to logged-in faculty
    private void verifyFacultyExam(
            String username, int examId) {

        if (examId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid exam ID");
        }

        String sql = """
            SELECT COUNT(*)
            FROM exams e
            JOIN subjects s ON e.subject_id = s.subject_id
            JOIN users u ON u.faculty_id = s.faculty_id
            WHERE e.exam_id = ?
              AND u.username = ?
              AND u.role = 'FACULTY'
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, examId, username);

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized for this exam");
        }
    }

    // 12. Get students and existing marks for an exam
    @GetMapping("/exam/students")
    public List<Map<String, Object>> getExamStudents(
            @RequestParam("examId") int examId,
            Authentication authentication) {

        verifyFacultyExam(
                authentication.getName(), examId);

        String sql = """
            SELECT
                st.student_id,
                st.roll_no,
                st.name,
                e.exam_id,
                e.exam_name,
                e.max_marks,
                m.obtained_marks

            FROM exams e

            JOIN enrollments en
                ON en.subject_id = e.subject_id

            JOIN students st
                ON st.student_id = en.student_id

            LEFT JOIN marks m
                ON m.student_id = st.student_id
                AND m.exam_id = e.exam_id

            WHERE e.exam_id = ?

            ORDER BY st.roll_no
            """;

        return jdbcTemplate.queryForList(sql, examId);
    }

    // 13. Save or update student marks
    @PostMapping("/marks")
    public Map<String, Object> saveMarks(
            @RequestBody MarksRequest request,
            Authentication authentication) {

        if (request.studentId() <= 0 ||
                request.examId() <= 0 ||
                request.obtainedMarks() == null ||
                request.obtainedMarks().compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid student ID, exam ID or marks");
        }

        // Verify faculty owns the exam
        verifyFacultyExam(
                authentication.getName(),
                request.examId());

        // Get exam subject and maximum marks
        String examSql = """
            SELECT subject_id, max_marks
            FROM exams
            WHERE exam_id = ?
            """;

        Map<String, Object> exam =
                jdbcTemplate.queryForMap(
                        examSql, request.examId());

        int subjectId = ((Number)
                exam.get("subject_id")).intValue();

        // Safe conversion to BigDecimal
        BigDecimal maxMarks =
                new BigDecimal(exam.get("max_marks").toString());

        // Validate obtained marks
        if (request.obtainedMarks().compareTo(maxMarks) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Obtained marks cannot exceed maximum marks: "
                            + maxMarks);
        }

        // Verify student enrollment
        String enrollmentSql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
              AND subject_id = ?
            """;

        Integer enrollmentCount = jdbcTemplate.queryForObject(
                enrollmentSql,
                Integer.class,
                request.studentId(),
                subjectId);

        if (enrollmentCount == null || enrollmentCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student is not enrolled in this subject");
        }

        // Insert new marks or update existing marks
        String insertSql = """
            INSERT INTO marks
                (student_id, exam_id, obtained_marks)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                obtained_marks = VALUES(obtained_marks)
            """;

        jdbcTemplate.update(
                insertSql,
                request.studentId(),
                request.examId(),
                request.obtainedMarks());

        return Map.of(
                "message", "Marks saved successfully",
                "studentId", request.studentId(),
                "examId", request.examId(),
                "obtainedMarks", request.obtainedMarks(),
                "maxMarks", maxMarks);
    }

    // 14. Marks request record
    public record MarksRequest(
            int studentId,
            int examId,
            BigDecimal obtainedMarks) {
    }
}
