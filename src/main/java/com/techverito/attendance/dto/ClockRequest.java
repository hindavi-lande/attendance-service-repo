package com.techverito.attendance.dto;

import jakarta.validation.constraints.NotNull;

public record ClockRequest(
        @NotNull Long employeeId
) {
}
