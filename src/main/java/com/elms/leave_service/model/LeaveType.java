package com.elms.leave_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@Entity
@ToString
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name="max_days", nullable = false)
    private int maxDays;

    @Column(name="is_rollable", nullable = false)
    private boolean rollable;

    @Column(name="max_carry_forward", nullable = false)
    private int maxCarryForward;

}
