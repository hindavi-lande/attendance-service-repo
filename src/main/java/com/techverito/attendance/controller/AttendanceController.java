package com.techverito.attendance.controller;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.dto.ClockRequest;
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

    @GetMapping("/{employeeId}")
    public List<AttendanceEntryResponse> monthlyHistory(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return attendanceService.findMonthlyHistory(employeeId, month);
    }
}
