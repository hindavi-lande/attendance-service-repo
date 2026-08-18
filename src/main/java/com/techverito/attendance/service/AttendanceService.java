package com.techverito.attendance.service;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.dto.LeaveRequestCreate;
import com.techverito.attendance.dto.LeaveRequestDecision;
import com.techverito.attendance.dto.LeaveRequestResponse;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceEntryResponse clockIn(Long employeeId);

    AttendanceEntryResponse clockOut(Long employeeId);

    List<AttendanceEntryResponse> findMonthlyHistory(Long employeeId, YearMonth month);

    LeaveRequestResponse submitLeaveRequest(LeaveRequestCreate request);

    LeaveRequestResponse decideLeaveRequest(Long leaveRequestId, LeaveRequestDecision request);
}
