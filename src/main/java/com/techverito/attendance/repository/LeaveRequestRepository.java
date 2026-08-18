package com.techverito.attendance.repository;

import com.techverito.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Modifying
    @Query(value = "INSERT INTO leave_request_dates (leave_request_id, work_date, created_at, updated_at) " +
            "VALUES (:leaveRequestId, :workDate, now(), now())", nativeQuery = true)
    void insertLeaveRequestDate(
            @Param("leaveRequestId") Long leaveRequestId,
            @Param("workDate") LocalDate workDate
    );

    @Query(value = "SELECT lrd.work_date FROM leave_request_dates lrd " +
            "WHERE lrd.leave_request_id = :leaveRequestId ORDER BY lrd.work_date", nativeQuery = true)
    List<LocalDate> findLeaveDatesByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);
}
