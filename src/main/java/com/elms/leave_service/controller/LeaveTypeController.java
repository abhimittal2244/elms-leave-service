package com.elms.leave_service.controller;

import com.elms.leave_service.modelDto.LeaveTypeRequestDto;
import com.elms.leave_service.modelDto.LeaveTypeResponseDto;
import com.elms.leave_service.service.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave/leaveType")
public class LeaveTypeController {
    @Autowired
    LeaveTypeService leaveTypeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LeaveTypeResponseDto> createLeaveType(@RequestBody @Valid LeaveTypeRequestDto dto) {
        return ResponseEntity.ok(leaveTypeService.createLeaveType(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeResponseDto> updateLeaveType(@PathVariable int id, @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping
    public  ResponseEntity<List<LeaveTypeResponseDto>> getAllLeaveType() {
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

}
