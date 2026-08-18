package com.techverito.attendance.exception;

public class ForbiddenLeaveActionException extends RuntimeException {

    public ForbiddenLeaveActionException(String message) {
        super(message);
    }
}
