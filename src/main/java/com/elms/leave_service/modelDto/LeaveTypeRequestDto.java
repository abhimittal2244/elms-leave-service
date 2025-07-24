package com.elms.leave_service.modelDto;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

//@Getter
@Data
@ToString
public class LeaveTypeRequestDto {
    private String name;
    private int maxDays;
    private boolean rollable;
    private int maxCarryForward;
}
