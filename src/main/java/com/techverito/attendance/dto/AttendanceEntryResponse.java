package com.techverito.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceEntryResponse(
        Long id,
        Long employeeId,
        LocalDate workDate,
        LocalDateTime clockInAt,
        LocalDateTime clockOutAt
) {
}
