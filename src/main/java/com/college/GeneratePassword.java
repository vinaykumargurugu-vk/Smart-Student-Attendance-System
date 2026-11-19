package com.college;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("ADMIN = " + encoder.encode("Admin@123"));
        System.out.println("FACULTY = " + encoder.encode("Faculty@123"));
        System.out.println("STUDENT = " + encoder.encode("Student@123"));
    }
}