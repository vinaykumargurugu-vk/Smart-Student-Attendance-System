
package com.college.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.college.config.DBConnection;

public class MarksDAO {

    // 1. Add Marks
    public void addMarks(String rollNo, int examId, double obtainedMarks) {

        String checkSql = """
            SELECT s.student_id, e.max_marks
            FROM students s
            JOIN exams e
            WHERE s.roll_no = ? AND e.exam_id = ?
            """;

        String insertSql = """
            INSERT INTO marks (student_id, exam_id, obtained_marks)
            VALUES (?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable!");
                return;
            }

            int studentId;
            double maxMarks;

            // Check Student and Exam
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {

                checkPs.setString(1, rollNo);
                checkPs.setInt(2, examId);

                try (ResultSet rs = checkPs.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("Student or exam not found!");
                        return;
                    }

                    studentId = rs.getInt("student_id");
                    maxMarks = rs.getDouble("max_marks");
                }
            }

            // Validate Marks
            if (obtainedMarks < 0 || obtainedMarks > maxMarks) {

                System.out.println(
                    "Invalid marks! Enter marks between 0 and " + maxMarks
                );

                return;
            }

            // Insert Marks
            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {

                insertPs.setInt(1, studentId);
                insertPs.setInt(2, examId);
                insertPs.setDouble(3, obtainedMarks);

                int rows = insertPs.executeUpdate();

                if (rows > 0) {
                    System.out.println("Marks added successfully!");
                }
            }

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                System.out.println(
                    "Marks already entered for this student and exam!"
                );

            } else {

                System.out.println("Error adding marks!");
                e.printStackTrace();
            }
        }
    }

    // 2. Display Student Marks
    public void displayStudentMarks(String rollNo) {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_name,
                e.exam_name,
                e.max_marks,
                m.obtained_marks,
                ROUND(
                    (m.obtained_marks * 100.0) / e.max_marks, 2
                ) AS percentage
            FROM marks m
            JOIN students s ON m.student_id = s.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            WHERE s.roll_no = ?
            ORDER BY sub.subject_name, e.exam_name
            """;

        Connection con = DBConnection.getConnection();

        if (con == null) {
            System.out.println("Database connection unavailable!");
            return;
        }

        try (con;
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== STUDENT MARKS REPORT =====");

                while (rs.next()) {

                    found = true;

                    System.out.println("Roll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Student Name: "
                            + rs.getString("name"));

                    System.out.println("Subject: "
                            + rs.getString("subject_name"));

                    System.out.println("Exam: "
                            + rs.getString("exam_name"));

                    System.out.println("Maximum Marks: "
                            + rs.getDouble("max_marks"));

                    System.out.println("Obtained Marks: "
                            + rs.getDouble("obtained_marks"));

                    System.out.println("Percentage: "
                            + rs.getDouble("percentage") + "%");

                    System.out.println("--------------------------------");
                }

                if (!found) {
                    System.out.println("No marks found for this student!");
                }
            }

        } catch (SQLException e) {

            System.out.println("Error retrieving student marks!");
            e.printStackTrace();
        }
    }
}
