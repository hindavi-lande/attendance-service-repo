package com.techverito.attendance.service.impl;

import com.techverito.attendance.dto.LeaveDecisionRequest;
import com.techverito.attendance.dto.LeaveRequestCreateRequest;
import com.techverito.attendance.dto.LeaveRequestResponse;
import com.techverito.attendance.entity.AttendanceEntry;
import com.techverito.attendance.entity.LeaveRequest;
import com.techverito.attendance.exception.ForbiddenLeaveActionException;
import com.techverito.attendance.exception.LeaveConflictException;
import com.techverito.attendance.exception.ResourceNotFoundException;
import com.techverito.attendance.repository.AttendanceEntryRepository;
import com.techverito.attendance.repository.LeaveRequestRepository;
import com.techverito.attendance.service.AttendanceServiceExternalDataProvider;
import com.techverito.attendance.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceEntryRepository attendanceEntryRepository;
    private final AttendanceServiceExternalDataProvider attendanceServiceExternalDataProvider;

    @Override
    @Transactional
    public LeaveRequestResponse submitLeaveRequest(LeaveRequestCreateRequest request) {
        LocalDate fromDate = request.fromDate();
        LocalDate toDate = request.toDate();

        if (toDate.isBefore(fromDate)) {
            throw new LeaveConflictException("toDate must be on or after fromDate");
        }

        LeaveRequest entity = LeaveRequest.builder()
                .employeeId(request.employeeId())
                .fromDate(fromDate)
                .toDate(toDate)
                .reason(request.reason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .managerId(null)
                .managerComment(null)
                .decidedAt(null)
                .build();

        return toResponse(leaveRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public LeaveRequestResponse decideLeaveRequest(Long leaveRequestId, LeaveDecisionRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new LeaveConflictException("Leave request has already been decided");
        }

        // For now, treat manager identity as the caller-provided managerId embedded in the external provider.
        // This keeps the acceptance rule for manager-only actions, while allowing integration to supply mapping.
        Long callerManagerId = attendanceServiceExternalDataProvider.findManagerIdForEmployee(leaveRequest.getEmployeeId());
        if (callerManagerId == null) {
            throw new ForbiddenLeaveActionException("No manager mapping exists for employee " + leaveRequest.getEmployeeId());
        }

        if (leaveRequest.getManagerId() != null && !leaveRequest.getManagerId().equals(callerManagerId)) {
            throw new ForbiddenLeaveActionException("Only assigned manager may decide this request");
        }

        // Require manager's comment when rejecting (and we also enforce on approve for auditing consistency)
        if (request.approved() == false && (request.comment() == null || request.comment().isBlank())) {
            throw new LeaveConflictException("Manager comment is required when rejecting a request");
        }

        LeaveRequest.LeaveStatus decidedStatus = request.approved() ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED;
        leaveRequest.setStatus(decidedStatus);
        leaveRequest.setManagerId(callerManagerId);
        leaveRequest.setManagerComment(request.comment());
        leaveRequest.setDecidedAt(LocalDateTime.now());

        if (decidedStatus == LeaveRequest.LeaveStatus.APPROVED) {
            // Mark each day as ON_LEAVE using distinct attendance status (do not infer from clock in/out)
            LocalDate current = leaveRequest.getFromDate();
            while (!current.isAfter(leaveRequest.getToDate())) {
                AttendanceEntry attendanceEntry = attendanceEntryRepository
                        .findByEmployeeIdAndWorkDate(leaveRequest.getEmployeeId(), current)
                        .orElse(null);

                if (attendanceEntry == null) {
                    attendanceEntry = AttendanceEntry.builder()
                            .employeeId(leaveRequest.getEmployeeId())
                            .workDate(current)
                            .attendanceStatus(AttendanceEntry.AttendanceStatus.ON_LEAVE)
                            .clockInAt(LocalDateTime.MIN)
                            .clockOutAt(null)
                            .build();
                } else {
                    attendanceEntry.setAttendanceStatus(AttendanceEntry.AttendanceStatus.ON_LEAVE);
                    attendanceEntry.setClockInAt(LocalDateTime.MIN);
                    attendanceEntry.setClockOutAt(null);
                }

                attendanceEntryRepository.save(attendanceEntry);

                current = current.plusDays(1);
            }
        }

        // Notification hook (intentionally not implemented here since no outbound integration exists in this slice)

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    private LeaveRequestResponse toResponse(LeaveRequest leaveRequest) {
        return new LeaveRequestResponse(
                leaveRequest.getId(),
                leaveRequest.getEmployeeId(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getReason(),
                leaveRequest.getStatus(),
                leaveRequest.getManagerId(),
                leaveRequest.getManagerComment(),
                leaveRequest.getSubmittedAt(),
                leaveRequest.getDecidedAt()
        );
    }
}
