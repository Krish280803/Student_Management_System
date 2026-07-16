package com.studentmanagement.exception;

public class DuplicateTeacherException extends RuntimeException {
    public DuplicateTeacherException(String message) {
        super(message);
    }
}
