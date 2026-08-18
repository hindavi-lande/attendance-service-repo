package com.techverito.attendance.service.impl;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.dto.LeaveRequestCreate;
import com.techverito.attendance.dto.LeaveRequestDecision;
import com.techverito.attendance.dto.LeaveRequestResponse;
import com.techverito.attendance.entity.AttendanceEntry;
import com.techverito.attendance.entity.LeaveRequest;
import com.techverito.attendance.exception.AttendanceConflictException;
import com.techverito.attendance.exception.ResourceNotFoundException;
import com.techverito.attendance.repository.AttendanceEntryRepository;
import com.techverito.attendance.repository.LeaveRequestRepository;
import com.techverito.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceEntryRepository attendanceEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public AttendanceEntryResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();

        attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today).ifPresent(existing -> {
            throw new AttendanceConflictException(
                    "Employee " + employeeId + " has already clocked in today");
        });

        AttendanceEntry entry = AttendanceEntry.builder()
                .employeeId(employeeId)
                .workDate(today)
                .clockInAt(LocalDateTime.now())
                .build();

        return toResponse(attendanceEntryRepository.save(entry));
    }

    @Override
    public AttendanceEntryResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        AttendanceEntry entry = attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee " + employeeId + " has not clocked in today"));

        if (entry.getClockOutAt() != null) {
            throw new AttendanceConflictException(
                    "Employee " + employeeId + " has already clocked out today");
        }

        entry.setClockOutAt(LocalDateTime.now());
        return toResponse(attendanceEntryRepository.save(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceEntryResponse> findMonthlyHistory(Long employeeId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        return attendanceEntryRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDate(employeeId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LeaveRequestResponse submitLeaveRequest(LeaveRequestCreate request) {
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(request.employeeId())
                .requestedFrom(request.requestedFrom())
                .requestedTo(request.requestedTo())
                .reason(request.reason())
                .status(LeaveRequest.Status.PENDING.name())
                .managerId(null)
                .managerComment(null)
                .approvedAt(null)
                .rejectedAt(null)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Create leave dates upfront so approvals can materialize attendance records.
        // (Actual attendance marking happens when manager decides.)
        for (LocalDate d = saved.getRequestedFrom(); !d.isAfter(saved.getRequestedTo()); d = d.plusDays(1)) {
            leaveRequestRepository.insertLeaveRequestDate(saved.getId(), d);
        }

        // Notification is a placeholder (no notification infrastructure provided in this slice).
        // The decision endpoint will also perform a notification placeholder.

        return toResponse(saved);
    }

    @Override
    public LeaveRequestResponse decideLeaveRequest(Long leaveRequestId, LeaveRequestDecision request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request " + leaveRequestId + " not found"));

        if (!LeaveRequest.Status.PENDING.name().equals(leaveRequest.getStatus())) {
            throw new AttendanceConflictException(
                    "Leave request " + leaveRequestId + " has already been decided");
        }

        leaveRequest.setManagerId(request.managerId());
        leaveRequest.setManagerComment(request.comment());

        LocalDateTime now = LocalDateTime.now();
        if (request.decision().equals(LeaveRequestDecision.Decision.APPROVE)) {
            leaveRequest.setStatus(LeaveRequest.Status.APPROVED.name());
            leaveRequest.setApprovedAt(now);
            leaveRequest.setRejectedAt(null);

            // Mark attendance as On Leave for each approved date.
            List<LocalDate> leaveDates = leaveRequestRepository.findLeaveDatesByLeaveRequestId(leaveRequestId);
            for (LocalDate d : leaveDates) {
                AttendanceEntry onLeave = attendanceEntryRepository
                        .findByEmployeeIdAndWorkDate(leaveRequest.getEmployeeId(), d)
                        .orElseGet(() -> AttendanceEntry.builder()
                                .employeeId(leaveRequest.getEmployeeId())
                                .workDate(d)
                                .build());

                // Use the clock_out_at field to signal absence while preserving schema.
                // - clock_in_at is set to now (or preserved) so row exists.
                // - clock_out_at is used as a marker when On Leave.
                if (onLeave.getClockInAt() == null) {
                    onLeave.setClockInAt(now);
                }
                onLeave.setClockOutAt(now);

                attendanceEntryRepository.save(onLeave);
            }

            // Notification placeholder for manager decision.
            // (No notification service defined in provided files.)
        } else {
            leaveRequest.setStatus(LeaveRequest.Status.REJECTED.name());
            leaveRequest.setRejectedAt(now);
            leaveRequest.setApprovedAt(null);

            // Notification placeholder for manager decision.
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    private AttendanceEntryResponse toResponse(AttendanceEntry entry) {
        return new AttendanceEntryResponse(
                entry.getId(),
                entry.getEmployeeId(),
                entry.getWorkDate(),
                entry.getClockInAt(),
                entry.getClockOutAt()
        );
    }

    private LeaveRequestResponse toResponse(LeaveRequest entry) {
        return new LeaveRequestResponse(
                entry.getId(),
                entry.getEmployeeId(),
                entry.getRequestedFrom(),
                entry.getRequestedTo(),
                entry.getReason(),
                entry.getStatus(),
                entry.getManagerId(),
                entry.getManagerComment(),
                entry.getApprovedAt(),
                entry.getRejectedAt(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
