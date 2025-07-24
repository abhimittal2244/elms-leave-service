package com.elms.leave_service.repository;

import com.elms.leave_service.enums.LeaveStatus;
import com.elms.leave_service.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByEmployeeId(int employeeId);

    List<LeaveRequest> findByEmployeeIdIn(List<Integer> employeeIds);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LeaveRequest l " +
            "WHERE l.employeeId = :employeeId " +
            "AND l.leaveStatus IN :statuses " +
            "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    boolean existsByEmployeeIdAndLeaveStatusInAndDateRangeOverlap(
            @Param("employeeId") int employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
