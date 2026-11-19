
package com.college.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final JdbcTemplate jdbcTemplate;

    public PerformanceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Common SQL for overall student performance
    private String getOverallPerformanceSql() {

        return """
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
    }

    // 1. Overall Student Performance
    @GetMapping("/overall")
    public List<Map<String, Object>> getOverallPerformance() {

        String sql = getOverallPerformanceSql();

        return addGrades(jdbcTemplate.queryForList(sql));
    }


    // 2. Low Performance Alerts (Below 40%)
    @GetMapping("/low-performance")
    public List<Map<String, Object>> getLowPerformanceAlerts() {

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
            HAVING
                SUM(m.obtained_marks) * 100.0 /
                NULLIF(SUM(e.max_marks), 0) < 40
            ORDER BY percentage ASC
            """;

        return addGrades(jdbcTemplate.queryForList(sql));
    }


    // 3. Subject-wise Performance
    @GetMapping("/subject-wise")
    public List<Map<String, Object>> getSubjectWisePerformance() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_name,
                SUM(m.obtained_marks) AS total_obtained,
                SUM(e.max_marks) AS total_maximum,
                ROUND(
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0), 2
                ) AS percentage
            FROM students s
            JOIN marks m ON s.student_id = m.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            GROUP BY
                s.student_id,
                s.roll_no,
                s.name,
                sub.subject_id,
                sub.subject_name
            ORDER BY s.roll_no, sub.subject_name
            """;

        return addGrades(jdbcTemplate.queryForList(sql));
    }


    // 4. Top 5 Performing Students
    @GetMapping("/top-performers")
    public List<Map<String, Object>> getTopPerformers() {

        List<Map<String, Object>> records =
                getOverallPerformance();

        int limit = Math.min(5, records.size());

        return new ArrayList<>(records.subList(0, limit));
    }


    // 5. Performance Summary
    @GetMapping("/summary")
    public Map<String, Object> getPerformanceSummary() {

        List<Map<String, Object>> records =
                getOverallPerformance();

        int totalStudents = records.size();

        double totalPercentage = 0;
        double highestPercentage = 0;
        int lowPerformers = 0;

        String topStudentName = "N/A";
        String topStudentRollNo = "N/A";

        for (int i = 0; i < records.size(); i++) {

            Map<String, Object> student = records.get(i);

            double percentage = ((Number)
                    student.get("percentage")).doubleValue();

            totalPercentage += percentage;

            if (percentage > highestPercentage || i == 0) {
                highestPercentage = percentage;
            }

            if (percentage < 40) {
                lowPerformers++;
            }

            if (i == 0) {
                topStudentName = student.get("name").toString();
                topStudentRollNo = student.get("roll_no").toString();
            }
        }

        double averagePercentage = totalStudents == 0
                ? 0
                : totalPercentage / totalStudents;

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("total_students_evaluated", totalStudents);
        summary.put("average_percentage",
                Math.round(averagePercentage * 100.0) / 100.0);
        summary.put("highest_percentage", highestPercentage);
        summary.put("low_performers_count", lowPerformers);
        summary.put("top_student_name", topStudentName);
        summary.put("top_student_roll_no", topStudentRollNo);

        return summary;
    }


    // 6. Add Grades to Performance Results
    private List<Map<String, Object>> addGrades(
            List<Map<String, Object>> records) {

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> record : records) {

            Map<String, Object> updatedRecord =
                    new LinkedHashMap<>(record);

            Object percentageValue = record.get("percentage");

            double percentage = percentageValue == null
                    ? 0
                    : ((Number) percentageValue).doubleValue();

            updatedRecord.put(
                    "grade",
                    calculateGrade(percentage)
            );

            result.add(updatedRecord);
        }

        return result;
    }


    // 7. Grade Calculation
    private String calculateGrade(double percentage) {

        if (percentage >= 90) {
            return "A+";
        } else if (percentage >= 80) {
            return "A";
        } else if (percentage >= 70) {
            return "B";
        } else if (percentage >= 60) {
            return "C";
        } else if (percentage >= 50) {
            return "D";
        } else if (percentage >= 40) {
            return "E";
        } else {
            return "F";
        }
    }
}
