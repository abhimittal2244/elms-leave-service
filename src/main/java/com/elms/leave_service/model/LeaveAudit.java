package com.elms.leave_service.model;

import com.elms.leave_service.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
public class LeaveAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequestId;

    @Column(name = "leave_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveStatus leaveStatus;

    @Column(nullable = false)
    private String comments;

    @Column(name = "changed_by", nullable = false)
    private int changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
