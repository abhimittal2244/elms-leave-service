package com.elms.leave_service.repository;

import com.elms.leave_service.model.LeaveAudit;
import com.elms.leave_service.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveAuditRepository extends JpaRepository<LeaveAudit, Integer> {
    List<LeaveAudit> findByLeaveRequestId(LeaveRequest leaveRequestId);
}
