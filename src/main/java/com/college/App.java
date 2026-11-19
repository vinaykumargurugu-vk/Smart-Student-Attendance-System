
package com.college;

import java.util.Scanner;
import java.time.LocalDate;

import com.college.dao.StudentDAO;
import com.college.dao.AttendanceDAO;
import com.college.dao.MarksDAO;
import com.college.dao.PerformanceDAO;

public class App {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // DAO Objects
        StudentDAO studentDAO = new StudentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        MarksDAO marksDAO = new MarksDAO();
        PerformanceDAO performanceDAO = new PerformanceDAO();

        int choice;

        do {

            System.out.println("\n================================");
            System.out.println(" SMART STUDENT MANAGEMENT SYSTEM");
            System.out.println("================================");

            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Mark Attendance");
            System.out.println("7. View Attendance Records");
            System.out.println("8. View Attendance Percentage");
            System.out.println("9. View Attendance Shortage Alerts");
            System.out.println("10. Add Student Marks");
            System.out.println("11. View Student Marks");
            System.out.println("12. View Overall Student Performance");
            System.out.println("13. View Low Performance Alerts");
            System.out.println("14. View Subject-wise Performance");
            System.out.println("15. Exit");

            System.out.print("\nEnter your choice: ");

            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {

                // 1. Add Student
                case 1: {

                    System.out.print("Enter Roll Number: ");
                    String rollNo = sc.nextLine();

                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter Department: ");
                    String department = sc.nextLine();

                    System.out.print("Enter Year: ");
                    int year = Integer.parseInt(sc.nextLine());

                    System.out.print("Enter Phone: ");
                    String phone = sc.nextLine();

                    studentDAO.addStudent(
                            rollNo, name, email,
                            department, year, phone
                    );

                    break;
                }

                // 2. View All Students
                case 2: {

                    studentDAO.displayAllStudents();

                    break;
                }

                // 3. Search Student
                case 3: {

                    System.out.print("Enter Roll Number: ");
                    String rollNo = sc.nextLine();

                    studentDAO.searchStudentByRollNo(rollNo);

                    break;
                }

                // 4. Update Student
                case 4: {

                    System.out.print("Enter Roll Number: ");
                    String rollNo = sc.nextLine();

                    System.out.print("Enter New Email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter New Department: ");
                    String department = sc.nextLine();

                    System.out.print("Enter New Year: ");
                    int year = Integer.parseInt(sc.nextLine());

                    System.out.print("Enter New Phone: ");
                    String phone = sc.nextLine();

                    studentDAO.updateStudent(
                            rollNo, email, department, year, phone
                    );

                    break;
                }

                // 5. Delete Student
                case 5: {

                    System.out.print("Enter Roll Number to Delete: ");
                    String rollNo = sc.nextLine();

                    studentDAO.deleteStudent(rollNo);

                    break;
                }

                // 6. Mark Attendance
                case 6: {

                    System.out.print("Enter Student Roll Number: ");
                    String rollNo = sc.nextLine();

                    System.out.print("Enter Subject Code: ");
                    String subjectCode = sc.nextLine();

                    System.out.print("Enter Date (YYYY-MM-DD): ");
                    LocalDate date = LocalDate.parse(sc.nextLine());

                    System.out.print("Enter Status (PRESENT/ABSENT): ");
                    String status = sc.nextLine().toUpperCase();

                    attendanceDAO.markAttendance(
                            rollNo, subjectCode, date, status
                    );

                    break;
                }

                // 7. View Attendance Records
                case 7: {

                    attendanceDAO.displayAttendanceRecords();

                    break;
                }

                // 8. View Attendance Percentage
                case 8: {

                    attendanceDAO.displayAttendancePercentage();

                    break;
                }

                // 9. Attendance Shortage Alerts
                case 9: {

                    attendanceDAO.displayAttendanceShortage();

                    break;
                }

                // 10. Add Student Marks
                case 10: {

                    System.out.print("Enter Student Roll Number: ");
                    String rollNo = sc.nextLine();

                    System.out.print("Enter Exam ID: ");
                    int examId = Integer.parseInt(sc.nextLine());

                    System.out.print("Enter Obtained Marks: ");
                    double obtainedMarks =
                            Double.parseDouble(sc.nextLine());

                    marksDAO.addMarks(
                            rollNo, examId, obtainedMarks
                    );

                    break;
                }

                // 11. View Student Marks
                case 11: {

                    System.out.print("Enter Student Roll Number: ");
                    String rollNo = sc.nextLine();

                    marksDAO.displayStudentMarks(rollNo);

                    break;
                }

                // 12. Overall Student Performance
                case 12: {

                    performanceDAO.displayOverallPerformance();

                    break;
                }

                // 13. Low Performance Alerts
                case 13: {

                    
                    performanceDAO.displayLowPerformanceAlerts();

                    break;
                }

                // 14. Subject-wise Performance
                case 14: {

                    performanceDAO.displaySubjectWisePerformance();

                    break;
                }

                // 15. Exit
                case 15: {

                    System.out.println(
                            "Thank you for using our system!"
                    );

                    break;
                }

                default: {

                    System.out.println("Invalid choice! Try again.");

                    break;
                }
            }

        } while (choice != 15);

        sc.close();
    }
}
