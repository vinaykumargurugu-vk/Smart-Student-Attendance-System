
package com.college.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String facultyPassword = "Faculty@12345";
        String studentPassword = "Student@12345";

        String facultyHash = encoder.encode(facultyPassword);
        String studentHash = encoder.encode(studentPassword);

        System.out.println("FACULTY HASH:");
        System.out.println(facultyHash);
        System.out.println("Faculty length = " + facultyHash.length());

        System.out.println("--------------------");

        System.out.println("STUDENT HASH:");
        System.out.println(studentHash);
        System.out.println("Student length = " + studentHash.length());

        System.out.println("--------------------");

        System.out.println("Faculty verification = " +
                encoder.matches(facultyPassword, facultyHash));

        System.out.println("Student verification = " +
                encoder.matches(studentPassword, studentHash));
    }
}
