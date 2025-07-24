package com.elms.leave_service.modelDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveBalanceResponseDto {
    private int leaveTypeId;
    private String leaveTypeName;
    private double availableBalance;
}
