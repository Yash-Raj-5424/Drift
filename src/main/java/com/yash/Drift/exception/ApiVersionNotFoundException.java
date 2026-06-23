package com.yash.Drift.exception;

public class ApiVersionNotFoundException extends RuntimeException {

    public ApiVersionNotFoundException(String message) {
        super(message);
    }
}
