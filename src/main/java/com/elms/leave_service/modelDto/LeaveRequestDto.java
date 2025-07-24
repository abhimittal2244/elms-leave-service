package com.elms.leave_service.modelDto;

import com.elms.leave_service.enums.DayType;
import com.elms.leave_service.enums.LeaveStatus;
import com.elms.leave_service.model.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    private int leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private DayType dayType;
}
