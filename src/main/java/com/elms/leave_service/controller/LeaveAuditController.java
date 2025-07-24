package com.elms.leave_service.controller;

import com.elms.leave_service.model.LeaveAudit;
import com.elms.leave_service.modelDto.LeaveAuditResponseDto;
import com.elms.leave_service.service.LeaveAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leave/leaveAudits")
public class LeaveAuditController {
    @Autowired
    private LeaveAuditService leaveAuditService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<LeaveAuditResponseDto>> getAllLeaveAudit() {
        return ResponseEntity.ok(leaveAuditService.getAllLeaveAudit());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<List<LeaveAudit>> getAllLeaveAuditByRequestId(@PathVariable int id) {
        return ResponseEntity.ok(leaveAuditService.getAllLeaveAuditByRequestId(id));
    }

}
