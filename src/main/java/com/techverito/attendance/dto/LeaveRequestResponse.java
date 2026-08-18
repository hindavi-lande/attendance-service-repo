package com.techverito.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        LocalDate requestedFrom,
        LocalDate requestedTo,
        String reason,
        String status,
        Long managerId,
        String managerComment,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
