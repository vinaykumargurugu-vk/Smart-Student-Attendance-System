
package com.college.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // MySQL database details
    private static final String URL =
            "jdbc:mysql://localhost:3306/student_management";

    private static final String USER = "root";

    private static final String PASSWORD = "vinay";

    // Method to establish database connection
    public static Connection getConnection() {

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            System.out.println("Database connected successfully!");

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

        }

        return connection;
    }
}
