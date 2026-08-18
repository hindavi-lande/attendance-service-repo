package com.techverito.attendance.dto;

import com.techverito.attendance.entity.LeaveRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        String reason,
        LeaveRequest.LeaveStatus status,
        Long managerId,
        String managerComment,
        LocalDateTime submittedAt,
        LocalDateTime decidedAt
) {
}
