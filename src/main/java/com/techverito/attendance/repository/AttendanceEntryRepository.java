package com.techverito.attendance.repository;

import com.techverito.attendance.entity.AttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceEntryRepository extends JpaRepository<AttendanceEntry, Long> {

    Optional<AttendanceEntry> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<AttendanceEntry> findByEmployeeIdAndWorkDateBetweenOrderByWorkDate(
            Long employeeId, LocalDate from, LocalDate to);
}
