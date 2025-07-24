package com.elms.leave_service.modelDto;

import com.elms.leave_service.enums.LeaveStatus;
import lombok.Data;

@Data
public class LeaveDecisionDto {
    private LeaveStatus status;
    private String managerComment;
}
