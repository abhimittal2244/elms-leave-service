package com.elms.leave_service.modelDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeaveResponseDto {
    private int leaveId;
    private int employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dayType;
    private String reason;
    private String leaveStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
