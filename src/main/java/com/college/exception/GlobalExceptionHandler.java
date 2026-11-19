
package com.college.exception;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Invalid JSON request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        return ResponseEntity.badRequest().body(
            Map.of("error",
                "Invalid JSON request. Please check your input.")
        );
    }

    // 2. Duplicate or conflicting database records
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of("error",
                "Duplicate or conflicting data. Please check your records.")
        );
    }

    // 3. Other database errors
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseError(
            DataAccessException ex) {

        ex.printStackTrace();

        return ResponseEntity.status(
            HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
            Map.of("error",
                "A database error occurred. Please try again.")
        );
    }

    // 4. Handle ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(
            ex.getStatusCode().value()
        );

        String message = ex.getReason() != null
                ? ex.getReason()
                : "Request failed.";

        return ResponseEntity.status(status).body(
            Map.of("error", message)
        );
    }

    // 5. Invalid input values
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
            Map.of("error", "Invalid input: " + ex.getMessage())
        );
    }

    // 6. Unsupported HTTP method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity.status(
            HttpStatus.METHOD_NOT_ALLOWED
        ).body(
            Map.of("error",
                "This HTTP method is not supported for this endpoint.")
        );
    }

    // 7. Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception ex) {

        ex.printStackTrace();

        return ResponseEntity.status(
            HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
            Map.of("error",
                "Something went wrong. Please contact the administrator.")
        );
    }
}
