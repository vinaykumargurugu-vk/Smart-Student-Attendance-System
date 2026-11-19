
package com.college.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.college.config.DBConnection;

public class StudentDAO {

    // Method to display all students
    public void displayAllStudents() {

        String query = "SELECT * FROM students";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()
        ) {

            System.out.println("\n========== STUDENT DETAILS ==========");

            while (resultSet.next()) {

                int id = resultSet.getInt("student_id");
                String rollNo = resultSet.getString("roll_no");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String department = resultSet.getString("department");
                int year = resultSet.getInt("year_of_study");

                System.out.println("-------------------------------------");
                System.out.println("Student ID : " + id);
                System.out.println("Roll Number: " + rollNo);
                System.out.println("Name       : " + name);
                System.out.println("Email      : " + email);
                System.out.println("Department : " + department);
                System.out.println("Year       : " + year);
            }

            System.out.println("\nStudent records retrieved successfully!");

        } catch (SQLException e) {

            System.out.println("Error retrieving student records!");
            e.printStackTrace();
        }
    }

    // Method to add a new student
    public void addStudent(String rollNo, String name,
                           String email, String department,
                           int year, String phone) {

        String query = "INSERT INTO students " +
                "(roll_no, name, email, department, year_of_study, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query)
        ) {

            statement.setString(1, rollNo);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, department);
            statement.setInt(5, year);
            statement.setString(6, phone);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Student added successfully!");
            }

        } catch (SQLException e) {

            System.out.println("Unable to add student!");
            e.printStackTrace();
        }
    }

    // Method to search student by roll number
    public void searchStudentByRollNo(String rollNo) {

        String query = "SELECT * FROM students WHERE roll_no = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query);
            ResultSet resultSet = getSearchResult(statement, rollNo)
        ) {

            if (resultSet.next()) {

                System.out.println("\n========== STUDENT FOUND ==========");

                System.out.println("Student ID : " +
                        resultSet.getInt("student_id"));

                System.out.println("Roll Number: " +
                        resultSet.getString("roll_no"));

                System.out.println("Name       : " +
                        resultSet.getString("name"));

                System.out.println("Email      : " +
                        resultSet.getString("email"));

                System.out.println("Department : " +
                        resultSet.getString("department"));

                System.out.println("Year       : " +
                        resultSet.getInt("year_of_study"));

                System.out.println("Phone      : " +
                        resultSet.getString("phone"));

            } else {

                System.out.println("Student not found!");
            }

        } catch (SQLException e) {

            System.out.println("Error searching student!");
            e.printStackTrace();
        }
    }

    private ResultSet getSearchResult(
            PreparedStatement statement, String rollNo)
            throws SQLException {

        statement.setString(1, rollNo);
        return statement.executeQuery();
    }

    // Method to update student details
    public void updateStudent(String rollNo, String email,
                              String department, int year,
                              String phone) {

        String query = "UPDATE students SET email = ?, " +
                "department = ?, year_of_study = ?, phone = ? " +
                "WHERE roll_no = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query)
        ) {

            statement.setString(1, email);
            statement.setString(2, department);
            statement.setInt(3, year);
            statement.setString(4, phone);
            statement.setString(5, rollNo);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Student details updated successfully!");
            } else {
                System.out.println("Student not found!");
            }

        } catch (SQLException e) {

            System.out.println("Unable to update student details!");
            e.printStackTrace();
        }
    }

    // Method to delete a student
    public void deleteStudent(String rollNo) {

        String query = "DELETE FROM students WHERE roll_no = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(query)
        ) {

            statement.setString(1, rollNo);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Student not found!");
            }

        } catch (SQLException e) {

            System.out.println("Unable to delete student!");
            e.printStackTrace();
        }
    }
}
