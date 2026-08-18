package com.techverito.attendance.service.impl;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.entity.AttendanceEntry;
import com.techverito.attendance.exception.AttendanceConflictException;
import com.techverito.attendance.exception.ResourceNotFoundException;
import com.techverito.attendance.repository.AttendanceEntryRepository;
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
                .attendanceStatus(AttendanceEntry.AttendanceStatus.CLOCKED_IN)
                .clockInAt(LocalDateTime.now())
                .clockOutAt(null)
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

    private AttendanceEntryResponse toResponse(AttendanceEntry entry) {
        return new AttendanceEntryResponse(
                entry.getId(),
                entry.getEmployeeId(),
                entry.getWorkDate(),
                entry.getClockInAt(),
                entry.getClockOutAt()
        );
    }
}
