
package com.college.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.college.config.DBConnection;

public class DashboardDAO {

    // 1. Get Total Students
    public int getTotalStudents() {

        String sql = "SELECT COUNT(*) AS total_students FROM students";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable!");
                return 0;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("total_students");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving total students!");
            e.printStackTrace();
        }

        return 0;
    }


    // 2. Get Average Student Performance
    public double getAveragePerformance() {

        String sql = """
            SELECT ROUND(AVG(percentage), 2) AS average_performance
            FROM (
                SELECT
                    m.student_id,
                    SUM(m.obtained_marks) * 100.0 /
                    NULLIF(SUM(e.max_marks), 0) AS percentage
                FROM marks m
                JOIN exams e ON m.exam_id = e.exam_id
                GROUP BY m.student_id
            ) AS performance
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return 0;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("average_performance");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error calculating average performance!");
            e.printStackTrace();
        }

        return 0;
    }


    // 3. Display Top 5 Performers
    public void displayTopPerformers() {

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

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== TOP 5 PERFORMERS =====");

                while (rs.next()) {

                    found = true;

                    System.out.println(
                        rs.getString("roll_no") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDouble("percentage") + "%"
                    );
                }

                if (!found) {
                    System.out.println("No student marks available!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving top performers!");
            e.printStackTrace();
        }
    }


    // 4. Get Average Attendance
    public double getAverageAttendance() {

        String sql = """
            SELECT ROUND(
                SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END)
                * 100.0 / NULLIF(COUNT(*), 0), 2
            ) AS average_attendance
            FROM attendance
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return 0;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("average_attendance");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error calculating average attendance!");
            e.printStackTrace();
        }

        return 0;
    }


    // 5. Display Low Attendance Alerts
    public void displayLowAttendanceAlerts() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                ROUND(
                    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END)
                    * 100.0 / NULLIF(COUNT(a.status), 0), 2
                ) AS attendance_percentage
            FROM students s
            JOIN attendance a ON s.student_id = a.student_id
            GROUP BY s.student_id, s.roll_no, s.name
            HAVING attendance_percentage < 75
            ORDER BY attendance_percentage ASC
            """;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== LOW ATTENDANCE ALERTS =====");

                while (rs.next()) {

                    found = true;

                    System.out.println("\nRoll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Name: "
                            + rs.getString("name"));

                    System.out.println("Attendance: "
                            + rs.getDouble("attendance_percentage") + "%");

                    System.out.println("ALERT: Attendance below 75%!");
                }

                if (!found) {
                    System.out.println("No attendance shortage found!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking attendance alerts!");
            e.printStackTrace();
        }
    }


    // 6. Display Academic Alerts
    public void displayAcademicAlerts() {

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

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n===== ACADEMIC PERFORMANCE ALERTS =====");

                while (rs.next()) {

                    found = true;

                    System.out.println("\nRoll Number: "
                            + rs.getString("roll_no"));

                    System.out.println("Name: "
                            + rs.getString("name"));

                    System.out.println("Percentage: "
                            + rs.getDouble("percentage") + "%");

                    System.out.println("ALERT: Student needs improvement!");
                }

                if (!found) {
                    System.out.println("No academic alerts found!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking academic alerts!");
            e.printStackTrace();
        }
    }


    // 7. Display Complete Dashboard
    public void displayDashboard() {

        System.out.println("\n========================================");
        System.out.println("       STUDENT PERFORMANCE DASHBOARD");
        System.out.println("========================================");

        System.out.println("\nTotal Students: "
                + getTotalStudents());

        System.out.println("Average Performance: "
                + getAveragePerformance() + "%");

        System.out.println("Average Attendance: "
                + getAverageAttendance() + "%");

        System.out.println("\n========================================");

        displayTopPerformers();

        System.out.println("\n========================================");

        displayLowAttendanceAlerts();

        System.out.println("\n========================================");

        displayAcademicAlerts();

        System.out.println("\n========================================");
        System.out.println("       END OF DASHBOARD");
        System.out.println("========================================");
    }
        // 8. Main Method for Testing Dashboard
    public static void main(String[] args) {

        DashboardDAO dao = new DashboardDAO();

        dao.displayDashboard();
    }

}
