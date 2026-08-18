package com.techverito.attendance.controller;

import com.techverito.attendance.dto.LeaveDecisionRequest;
import com.techverito.attendance.dto.LeaveRequestCreateRequest;
import com.techverito.attendance.dto.LeaveRequestResponse;
import com.techverito.attendance.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public LeaveRequestResponse submit(@Valid @RequestBody LeaveRequestCreateRequest request) {
        return leaveService.submitLeaveRequest(request);
    }

    @PostMapping("/{leaveRequestId}/decision")
    public LeaveRequestResponse decide(
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody LeaveDecisionRequest request
    ) {
        return leaveService.decideLeaveRequest(leaveRequestId, request);
    }
}
