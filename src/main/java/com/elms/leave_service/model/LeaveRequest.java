package com.elms.leave_service.model;

import com.elms.leave_service.enums.DayType;
import com.elms.leave_service.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "employee_id", nullable = false)
    private int employeeId;

    @ManyToOne
    @JoinColumn(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String reason;

    @Column(name = "leave_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveStatus leaveStatus;

    @Column(name = "day_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DayType dayType;

//    @Column(name = "manager_comment", nullable = false)
    private String managerComment;

    @Column(name = "approved_by", nullable = false)
    private int approvedBy;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
