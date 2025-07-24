package com.elms.leave_service.modelDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class LeaveTypeResponseDto {
    private String name;
    private int maxDays;
    private boolean rollable;
    private int maxCarryForward;
}
