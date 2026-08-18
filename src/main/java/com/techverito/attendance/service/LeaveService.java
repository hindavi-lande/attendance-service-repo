package com.techverito.attendance.service;

import com.techverito.attendance.dto.LeaveDecisionRequest;
import com.techverito.attendance.dto.LeaveRequestCreateRequest;
import com.techverito.attendance.dto.LeaveRequestResponse;

public interface LeaveService {

    LeaveRequestResponse submitLeaveRequest(LeaveRequestCreateRequest request);

    LeaveRequestResponse decideLeaveRequest(Long leaveRequestId, LeaveDecisionRequest request);
}
