package com.elms.leave_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "employee_id", nullable = false)
    private int employeeId;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveTypeId;

    @Column(name = "available_balance", nullable = false)
    private double availableBalance;

    @Column(nullable = false)
    private int year;
}
