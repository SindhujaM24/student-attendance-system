package com.example.attendance.exception;

import org.springframework.http.HttpStatus;

/** Thrown from services/controllers when we want to send a clear error message to the browser. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
