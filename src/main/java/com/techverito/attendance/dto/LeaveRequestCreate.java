package com.techverito.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestCreate(
        @NotNull Long employeeId,
        @NotNull LocalDate requestedFrom,
        @NotNull LocalDate requestedTo,
        @NotNull @Size(min = 1, max = 2000) String reason
) {
}
