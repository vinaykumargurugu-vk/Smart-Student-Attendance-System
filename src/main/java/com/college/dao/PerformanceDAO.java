
package com.college.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.college.config.DBConnection;

public class PerformanceDAO {

    // 1. Display Overall Student Performance
    public void displayOverallPerformance() {

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

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable!");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== OVERALL STUDENT PERFORMANCE =====");

                while (rs.next()) {

                    found = true;

                    double percentage = rs.getDouble("percentage");

                    System.out.println("\nRoll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Student Name: "
                            + rs.getString("name"));

                    System.out.println("Total Obtained Marks: "
                            + rs.getDouble("total_obtained"));

                    System.out.println("Total Maximum Marks: "
                            + rs.getDouble("total_maximum"));

                    System.out.println("Overall Percentage: "
                            + percentage + "%");

                    System.out.println("Grade: "
                            + calculateGrade(percentage));

                    System.out.println("-----------------------------------");
                }

                if (!found) {
                    System.out.println("No student marks available!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error calculating overall performance!");
            e.printStackTrace();
        }
    }


    // 2. Display Low Performance Alerts
    public void displayLowPerformanceAlerts() {

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
            HAVING SUM(m.obtained_marks) * 100.0 /
                   NULLIF(SUM(e.max_marks), 0) < 40
            ORDER BY percentage ASC
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable!");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== LOW PERFORMANCE ALERTS =====");

                while (rs.next()) {

                    found = true;

                    System.out.println("\nRoll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Student Name: "
                            + rs.getString("name"));

                    System.out.println("Obtained Marks: "
                            + rs.getDouble("total_obtained"));

                    System.out.println("Maximum Marks: "
                            + rs.getDouble("total_maximum"));

                    System.out.println("Percentage: "
                            + rs.getDouble("percentage") + "%");

                    System.out.println("Grade: F");

                    System.out.println("ALERT: Student needs improvement!");

                    System.out.println("-----------------------------------");
                }

                if (!found) {
                    System.out.println(
                            "No low-performing students found!"
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking low performance!");
            e.printStackTrace();
        }
    }


    // 3. Display Subject-wise Performance
    public void displaySubjectWisePerformance() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                e.subject_name,
                SUM(m.obtained_marks) AS total_obtained,
                SUM(e.max_marks) AS total_maximum,
                ROUND(
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0), 2
                ) AS percentage
            FROM students s
            JOIN marks m ON s.student_id = m.student_id
            JOIN exams e ON m.exam_id = e.exam_id
            GROUP BY
                s.student_id,
                s.roll_no,
                s.name,
                e.subject_name
            ORDER BY s.roll_no, e.subject_name
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable!");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== SUBJECT-WISE PERFORMANCE =====");

                while (rs.next()) {

                    found = true;

                    double percentage = rs.getDouble("percentage");

                    System.out.println("\nRoll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Student Name: "
                            + rs.getString("name"));

                    System.out.println("Subject: "
                            + rs.getString("subject_name"));

                    System.out.println("Obtained Marks: "
                            + rs.getDouble("total_obtained"));

                    System.out.println("Maximum Marks: "
                            + rs.getDouble("total_maximum"));

                    System.out.println("Percentage: "
                            + percentage + "%");

                    System.out.println("Grade: "
                            + calculateGrade(percentage));

                    System.out.println("-----------------------------------");
                }

                if (!found) {
                    System.out.println("No subject-wise marks available!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error displaying subject-wise performance!");
            e.printStackTrace();
        }
    }


    // 4. Grade Calculation
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
