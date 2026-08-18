package com.techverito.attendance.service;

import com.techverito.attendance.dto.AttendanceEntryResponse;
import com.techverito.attendance.entity.AttendanceEntry;
import com.techverito.attendance.exception.AttendanceConflictException;
import com.techverito.attendance.exception.ResourceNotFoundException;
import com.techverito.attendance.repository.AttendanceEntryRepository;
import com.techverito.attendance.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceEntryRepository attendanceEntryRepository;

    private AttendanceService attendanceService;

    private AttendanceServiceImpl newService() {
        return new AttendanceServiceImpl(attendanceEntryRepository);
    }

    @Test
    void clockInCreatesAnEntryWhenNoneExistsForToday() {
        attendanceService = newService();
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        when(attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today))
                .thenReturn(Optional.empty());
        when(attendanceEntryRepository.save(any(AttendanceEntry.class)))
                .thenAnswer(invocation -> {
                    AttendanceEntry entry = invocation.getArgument(0);
                    entry.setId(100L);
                    return entry;
                });

        AttendanceEntryResponse response = attendanceService.clockIn(employeeId);

        assertThat(response.employeeId()).isEqualTo(employeeId);
        assertThat(response.workDate()).isEqualTo(today);
        assertThat(response.clockInAt()).isNotNull();
        assertThat(response.clockOutAt()).isNull();

        ArgumentCaptor<AttendanceEntry> captor = ArgumentCaptor.forClass(AttendanceEntry.class);
        verify(attendanceEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployeeId()).isEqualTo(employeeId);
    }

    @Test
    void clockInFailsWhenAlreadyClockedInToday() {
        attendanceService = newService();
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        AttendanceEntry existing = AttendanceEntry.builder()
                .id(1L)
                .employeeId(employeeId)
                .workDate(today)
                .attendanceStatus(AttendanceEntry.AttendanceStatus.CLOCKED_IN)
                .clockInAt(LocalDateTime.now())
                .build();

        when(attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.clockIn(employeeId))
                .isInstanceOf(AttendanceConflictException.class);

        verify(attendanceEntryRepository, never()).save(any());
    }

    @Test
    void clockOutFailsWhenNoClockInRecordedToday() {
        attendanceService = newService();
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        when(attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.clockOut(employeeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void clockOutSucceedsAfterClockIn() {
        attendanceService = newService();
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        AttendanceEntry existing = AttendanceEntry.builder()
                .id(1L)
                .employeeId(employeeId)
                .workDate(today)
                .attendanceStatus(AttendanceEntry.AttendanceStatus.CLOCKED_IN)
                .clockInAt(LocalDateTime.now().minusHours(8))
                .build();

        when(attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today))
                .thenReturn(Optional.of(existing));
        when(attendanceEntryRepository.save(any(AttendanceEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceEntryResponse response = attendanceService.clockOut(employeeId);

        assertThat(response.clockOutAt()).isNotNull();
    }

    @Test
    void clockOutFailsWhenAlreadyClockedOutToday() {
        attendanceService = newService();
        Long employeeId = 1L;
        LocalDate today = LocalDate.now();

        AttendanceEntry existing = AttendanceEntry.builder()
                .id(1L)
                .employeeId(employeeId)
                .workDate(today)
                .attendanceStatus(AttendanceEntry.AttendanceStatus.CLOCKED_IN)
                .clockInAt(LocalDateTime.now().minusHours(8))
                .clockOutAt(LocalDateTime.now())
                .build();

        when(attendanceEntryRepository.findByEmployeeIdAndWorkDate(employeeId, today))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.clockOut(employeeId))
                .isInstanceOf(AttendanceConflictException.class);
    }
}
