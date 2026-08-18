package com.techverito.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveRequestDecision(
        @NotNull Long managerId,
        @NotNull Decision decision,
        @Size(min = 1, max = 2000) String comment
) {

    public enum Decision {
        APPROVE,
        REJECT
    }
}
