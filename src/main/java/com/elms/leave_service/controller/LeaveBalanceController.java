package com.elms.leave_service.controller;

import com.elms.leave_service.modelDto.LeaveBalanceResponseDto;
import com.elms.leave_service.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leave/leaveBalance")
public class LeaveBalanceController {
    @Autowired
    private LeaveBalanceService leaveBalanceService;

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/init1")
    public void initializeLeaveBalance(@RequestParam int employeeId,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinDate) {
        leaveBalanceService.initializeLeaveBalance(employeeId, joinDate);
//        return ResponseEntity.ok("Leave Balance Initialized");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<LeaveBalanceResponseDto>> getLeaveBalanceByEmployee() {
        return ResponseEntity.ok(leaveBalanceService.getLeaveBalanceByEmployee());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/{leaveTypeId}")
    public ResponseEntity<LeaveBalanceResponseDto> getLeaveBalanceByEmployeeIdAndLeaveTypeId(@PathVariable int leaveTypeId) {
        return ResponseEntity.ok(leaveBalanceService.getLeaveBalanceByEmployeeIdAndLeaveTypeId(leaveTypeId));
    }

}
