package com.techverito.attendance.service;

import com.techverito.attendance.dto.AttendanceEntryResponse;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceEntryResponse clockIn(Long employeeId);

    AttendanceEntryResponse clockOut(Long employeeId);

    List<AttendanceEntryResponse> findMonthlyHistory(Long employeeId, YearMonth month);
}
