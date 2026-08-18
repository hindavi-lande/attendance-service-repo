package com.techverito.attendance.controller;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.dto.ClockRequest;
import com.techverito.attendance.dto.LeaveRequestCreate;
import com.techverito.attendance.dto.LeaveRequestDecision;
import com.techverito.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public AttendanceEntryResponse clockIn(@Valid @RequestBody ClockRequest request) {
        return attendanceService.clockIn(request.employeeId());
    }

    @PostMapping("/clock-out")
    public AttendanceEntryResponse clockOut(@Valid @RequestBody ClockRequest request) {
        return attendanceService.clockOut(request.employeeId());
    }

    @PostMapping("/leave-requests")
    public com.techverito.attendance.dto.LeaveRequestResponse submitLeaveRequest(
            @Valid @RequestBody LeaveRequestCreate request
    ) {
        return attendanceService.submitLeaveRequest(request);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/decision")
    public com.techverito.attendance.dto.LeaveRequestResponse decideLeaveRequest(
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody LeaveRequestDecision request
    ) {
        return attendanceService.decideLeaveRequest(leaveRequestId, request);
    }

    @GetMapping("/{employeeId}")
    public List<AttendanceEntryResponse> monthlyHistory(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return attendanceService.findMonthlyHistory(employeeId, month);
    }
}
