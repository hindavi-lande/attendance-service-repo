package com.techverito.attendance.exception;

public class LeaveConflictException extends RuntimeException {

    public LeaveConflictException(String message) {
        super(message);
    }
}
