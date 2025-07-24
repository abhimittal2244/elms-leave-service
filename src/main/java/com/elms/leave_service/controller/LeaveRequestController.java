package com.elms.leave_service.controller;

import com.elms.leave_service.feignClient.EmployeeClient;
import com.elms.leave_service.model.LeaveRequest;
import com.elms.leave_service.modelDto.LeaveDecisionDto;
import com.elms.leave_service.modelDto.LeaveRequestDto;
import com.elms.leave_service.modelDto.LeaveResponseDto;
import com.elms.leave_service.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave/leaveRequest")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EmployeeClient employeeClient;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<LeaveResponseDto> applyLeave(@RequestBody LeaveRequestDto leaveRequest) {
        return ResponseEntity.ok(leaveRequestService.applyLeave(leaveRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<LeaveResponseDto>> getAllAdminLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllAdminLeaveRequests());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<LeaveResponseDto>> getLeaveRequestsUnderManager(){
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsUnderManager());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/employeeRequest")
    public ResponseEntity<List<LeaveResponseDto>> getLeaveRequestsByEmployee() {
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsByEmployee());

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{leaveId}/decision")
    public ResponseEntity<String> decideLeave(@PathVariable int leaveId,
                                              @RequestBody LeaveDecisionDto dto) {

        return ResponseEntity.ok(leaveRequestService.updateLeaveStatus(leaveId, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{leaveId}/override")
    public ResponseEntity<String> overrideLeaveRequest(@PathVariable int leaveId,
                                                       @RequestBody LeaveDecisionDto dto) {
        System.out.println(leaveId + "--" + dto);
        return ResponseEntity.ok(leaveRequestService.overrideLeaveRequest(leaveId, dto));
    }
}
