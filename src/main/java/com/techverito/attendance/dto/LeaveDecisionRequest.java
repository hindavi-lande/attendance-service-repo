package com.techverito.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public record LeaveDecisionRequest(
        boolean approved,
        @NotBlank String comment
) {
}
