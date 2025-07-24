package com.elms.leave_service.repository;

import com.elms.leave_service.model.LeaveBalance;
import com.elms.leave_service.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {
    List<LeaveBalance> findByEmployeeIdAndYear(int employeeId, int year);
    boolean existsByEmployeeIdAndYear(int employeeId, int year);
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(int employeeId, LeaveType leaveTypeId, int year);
    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(int employeeId, LeaveType leaveTypeId, int year);
}
