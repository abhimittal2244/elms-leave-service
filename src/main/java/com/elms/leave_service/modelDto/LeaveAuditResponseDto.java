package com.elms.leave_service.modelDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeaveAuditResponseDto {
    private int auditId;
    private int leaveRequestId;
    private String leaveStatus;
    private String comments;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dayType;
    private String leaveType;
}
