package com.techverito.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank String reason
) {
}
