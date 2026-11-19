
package com.college.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.college.config.DBConnection;

public class AttendanceDAO {

    // Method to mark student attendance
    public void markAttendance(String rollNo,
                               String subjectCode,
                               LocalDate attendanceDate,
                               String status) {

        String query = "INSERT INTO attendance " +
                "(student_id, subject_id, attendance_date, status) " +
                "SELECT s.student_id, sub.subject_id, ?, ? " +
                "FROM students s " +
                "JOIN enrollments e ON s.student_id = e.student_id " +
                "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                "WHERE s.roll_no = ? AND sub.subject_code = ?";

        if (!status.equals("PRESENT") && !status.equals("ABSENT")) {
            System.out.println("Invalid attendance status!");
            return;
        }

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query)
        ) {

            statement.setDate(
                    1, java.sql.Date.valueOf(attendanceDate)
            );

            statement.setString(2, status);
            statement.setString(3, rollNo);
            statement.setString(4, subjectCode);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Attendance marked successfully!");
            } else {
                System.out.println(
                        "Student or subject not found, or student is not enrolled!"
                );
            }

        } catch (SQLException e) {

            System.out.println("Unable to mark attendance!");
            e.printStackTrace();
        }
    }

    // Method to display all attendance records
    public void displayAttendanceRecords() {

        String query =
                "SELECT s.roll_no, s.name, sub.subject_code, " +
                "sub.subject_name, a.attendance_date, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.student_id " +
                "JOIN subjects sub ON a.subject_id = sub.subject_id " +
                "ORDER BY a.attendance_date DESC, s.roll_no";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()
        ) {

            System.out.println("\n========== ATTENDANCE RECORDS ==========");

            boolean found = false;

            while (resultSet.next()) {

                found = true;

                System.out.println("----------------------------------------");

                System.out.println("Roll Number : " +
                        resultSet.getString("roll_no"));

                System.out.println("Student Name: " +
                        resultSet.getString("name"));

                System.out.println("Subject Code: " +
                        resultSet.getString("subject_code"));

                System.out.println("Subject Name: " +
                        resultSet.getString("subject_name"));

                System.out.println("Date        : " +
                        resultSet.getDate("attendance_date"));

                System.out.println("Status      : " +
                        resultSet.getString("status"));
            }

            if (!found) {
                System.out.println("No attendance records found!");
            }

        } catch (SQLException e) {

            System.out.println("Error retrieving attendance records!");
            e.printStackTrace();
        }
    }

    // Method to display attendance percentage
    public void displayAttendancePercentage() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                COUNT(a.attendance_id) AS total_classes,
                SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_classes,
                ROUND(
                    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END)
                    * 100.0 / COUNT(a.attendance_id), 2
                ) AS attendance_percentage
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN subjects sub ON a.subject_id = sub.subject_id
            GROUP BY
                s.student_id, s.roll_no, s.name,
                sub.subject_id, sub.subject_code, sub.subject_name
            ORDER BY s.roll_no, sub.subject_code
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            boolean found = false;

            System.out.println("\n----- ATTENDANCE PERCENTAGE -----");

            while (rs.next()) {

                found = true;

                System.out.println("Roll Number: " +
                        rs.getString("roll_no"));

                System.out.println("Student Name: " +
                        rs.getString("name"));

                System.out.println("Subject: " +
                        rs.getString("subject_name"));

                System.out.println("Total Classes: " +
                        rs.getInt("total_classes"));

                System.out.println("Present Classes: " +
                        rs.getInt("present_classes"));

                System.out.println("Attendance: " +
                        rs.getDouble("attendance_percentage") + "%");

                System.out.println("--------------------------------");
            }

            if (!found) {
                System.out.println("No attendance records found!");
            }

        } catch (SQLException e) {

            System.out.println("Error calculating attendance percentage!");
            e.printStackTrace();
        }
    }

    // Method to display attendance shortage
    public void displayAttendanceShortage() {

        String sql = """
            SELECT
                s.roll_no,
                s.name,
                sub.subject_code,
                sub.subject_name,
                COUNT(a.attendance_id) AS total_classes,
                SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_classes,
                ROUND(
                    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END)
                    * 100.0 / COUNT(a.attendance_id), 2
                ) AS attendance_percentage
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN subjects sub ON a.subject_id = sub.subject_id
            GROUP BY
                s.student_id, s.roll_no, s.name,
                sub.subject_id, sub.subject_code, sub.subject_name
            HAVING attendance_percentage < 75
            ORDER BY attendance_percentage ASC
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            boolean found = false;

            System.out.println("\n===== ATTENDANCE SHORTAGE ALERTS =====");

            while (rs.next()) {

                found = true;

                System.out.println("WARNING: LOW ATTENDANCE!");

                System.out.println("Roll Number: " +
                        rs.getString("roll_no"));

                System.out.println("Student Name: " +
                        rs.getString("name"));

                System.out.println("Subject: " +
                        rs.getString("subject_name"));

                System.out.println("Attendance: " +
                        rs.getDouble("attendance_percentage") + "%");

                System.out.println("--------------------------------");
            }

            if (!found) {
                System.out.println("No students have attendance below 75%.");
            }

        } catch (SQLException e) {

            System.out.println("Error checking attendance shortage!");
            e.printStackTrace();
        }
    }
}
