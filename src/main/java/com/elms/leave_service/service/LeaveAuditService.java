package com.elms.leave_service.service;

import com.elms.leave_service.enums.ErrorCode;
import com.elms.leave_service.enums.LeaveStatus;
import com.elms.leave_service.exceptions.ApiException;
import com.elms.leave_service.exceptions.ResourceNotFoundException;
import com.elms.leave_service.feignClient.EmployeeClient;
import com.elms.leave_service.model.LeaveAudit;
import com.elms.leave_service.model.LeaveRequest;
import com.elms.leave_service.modelDto.LeaveAuditResponseDto;
import com.elms.leave_service.repository.LeaveAuditRepository;
import com.elms.leave_service.repository.LeaveRequestRepository;
import com.elms.leave_service.util.UserContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveAuditService {
    @Autowired
    private LeaveAuditRepository leaveAuditRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeClient employeeClient;

    @Autowired
    private UserContext userContext;

    @Transactional
    public void addAuditLog(LeaveRequest leaveId, LeaveStatus leaveStatus,
                            String comments, int changedBy,
                            LocalDateTime updatedAt) {
            LeaveAudit newAudit = new LeaveAudit();
            newAudit.setLeaveRequestId(leaveId);
            newAudit.setLeaveStatus(leaveStatus);
            newAudit.setComments(comments);
            newAudit.setChangedBy(changedBy);
            newAudit.setChangedAt(updatedAt);

        try {
            leaveAuditRepository.save(newAudit);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.SQL_ERROR, ex.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public List<LeaveAuditResponseDto> getAllLeaveAudit() {
        List<LeaveAudit> audits = leaveAuditRepository.findAll();
        return audits.stream().map(audit -> {
            try {
                String changedBy = employeeClient.getEmployeeNameById(audit.getChangedBy()).getBody();
                return new LeaveAuditResponseDto(
                        audit.getId(),
                        audit.getLeaveRequestId().getId(),
                        audit.getLeaveRequestId().getLeaveStatus().toString(),
                        audit.getComments(),
                        changedBy,
                        audit.getChangedAt(),
                        audit.getLeaveRequestId().getStartDate(),
                        audit.getLeaveRequestId().getEndDate(),
                        audit.getLeaveRequestId().getDayType().toString(),
                        audit.getLeaveRequestId().getLeaveType().getName()
                );
            } catch (Exception ex) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
            }
        }).collect(Collectors.toList());

    }

    public List<LeaveAudit> getAllLeaveAuditByRequestId(int leaveId) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave Request", leaveId));
        return leaveAuditRepository.findByLeaveRequestId(request);
    }
}
