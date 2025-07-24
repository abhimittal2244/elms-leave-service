package com.elms.leave_service.repository;

import com.elms.leave_service.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
    Optional<LeaveType> findByNameIgnoreCase(String name);
}
