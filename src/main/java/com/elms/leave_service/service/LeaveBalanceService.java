package com.elms.leave_service.service;

import com.elms.leave_service.ClientDto.EmployeeMetadataDto;
import com.elms.leave_service.enums.ErrorCode;
import com.elms.leave_service.exceptions.ApiException;
import com.elms.leave_service.exceptions.ResourceNotFoundException;
import com.elms.leave_service.feignClient.EmployeeClient;
import com.elms.leave_service.model.LeaveBalance;
import com.elms.leave_service.model.LeaveType;
import com.elms.leave_service.modelDto.LeaveBalanceResponseDto;
import com.elms.leave_service.modelDto.LeaveTypeResponseDto;
import com.elms.leave_service.repository.LeaveBalanceRepository;
import com.elms.leave_service.repository.LeaveTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeClient employeeClient;

    @Transactional
    public void initializeLeaveBalance(int employeeId, LocalDate joinDate) {
        if (joinDate == null) {
            throw new ApiException(ErrorCode.EMPTY_DATA_RECEIVED, "Join Date is NULL");
        }
        int currentYear = LocalDate.now().getYear();

        boolean alreadyInitialized = leaveBalanceRepository.existsByEmployeeIdAndYear(employeeId, currentYear);
        System.out.println("isalreadyinitialized"+alreadyInitialized);
        if (!alreadyInitialized) {
            try {
                int joinYear = joinDate.getYear();
                int monthsRemaining;
                int joinMonth = joinDate.getMonthValue();
                if (joinYear < currentYear) {
                    monthsRemaining = 25 - joinMonth;
                } else {
                    monthsRemaining = 13 - joinMonth;
                }

                List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

                if (leaveTypes.isEmpty()) {
                    throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No Leave Type found");
                }

                for (LeaveType type : leaveTypes) {
                    double prorated = (monthsRemaining / 12.0) * type.getMaxDays();
                    LeaveBalance lb = new LeaveBalance();
                    lb.setEmployeeId(employeeId);
                    lb.setLeaveTypeId(type);
                    lb.setAvailableBalance(Math.floor(prorated));
                    lb.setYear(currentYear);

                    leaveBalanceRepository.save(lb);
                }
            } catch (DataIntegrityViolationException e) {
                throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, e.getMessage());
            }
        }
    }

    @Transactional
    public LeaveTypeResponseDto initializeAllEmployeesLeaveBalanceByLeaveType(LeaveType leaveType) {
        if (leaveType == null) {
            throw new ApiException(ErrorCode.EMPTY_DATA_RECEIVED, "Leave Type not Found");
        }
        try {
            List<EmployeeMetadataDto> employees = employeeClient.getAllApprovedEmployees().getBody();
            if (employees == null || employees.isEmpty()) {
                throw new ApiException(ErrorCode.NOT_FOUND, "No approved employees found to initialize leave balance");
            }
            int currentYear = LocalDate.now().getYear();

            for(EmployeeMetadataDto emp : employees) {
                if (emp == null || emp.getEmployeeId() <= 0) {
                    throw new ApiException(ErrorCode.INVALID_INPUT, "Invalid employee data encountered during leave balance initialization");
                }
                boolean exists = leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(emp.getEmployeeId(), leaveType, currentYear);
                if(!exists) {

                    int monthsRemaining = 13 - LocalDate.now().getMonthValue();

                    double prorated = (monthsRemaining/12.0)*leaveType.getMaxDays();

                    LeaveBalance balance = new LeaveBalance();
                    balance.setEmployeeId(emp.getEmployeeId());
                    balance.setLeaveTypeId(leaveType);
                    balance.setAvailableBalance(Math.floor(prorated));
                    balance.setYear(currentYear);
                    try {
                        leaveBalanceRepository.save(balance);
                    } catch (DataIntegrityViolationException e) {
                        throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
                    } catch (Exception ex) {
                        throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
                    }
                }
            }
            return new LeaveTypeResponseDto(
                    leaveType.getName(),
                    leaveType.getMaxDays(),
                    leaveType.isRollable(),
                    leaveType.getMaxCarryForward()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    public List<LeaveBalanceResponseDto> getLeaveBalanceByEmployee() {
        int currentYear = LocalDate.now().getYear();
        try {
            EmployeeMetadataDto employee = employeeClient.getEmployeeMetadata().getBody();

            assert employee != null;
            return leaveBalanceRepository.findByEmployeeIdAndYear(employee.getEmployeeId(), currentYear)
                    .stream().map(lb -> new LeaveBalanceResponseDto(
                            lb.getId(),
                            lb.getLeaveTypeId().getName(),
                            lb.getAvailableBalance()
                    )).toList();
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.SQL_ERROR, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    public LeaveBalanceResponseDto getLeaveBalanceByEmployeeIdAndLeaveTypeId(int leaveTypeId) {
        try {
            EmployeeMetadataDto employee = employeeClient.getEmployeeMetadata().getBody();
            LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Leave Type",leaveTypeId));
            assert employee != null;
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    employee.getEmployeeId(),
                    leaveType,
                    LocalDate.now().getYear()
            ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Unable to find LeaveBalance"));

            return new LeaveBalanceResponseDto(
                    balance.getLeaveTypeId().getId(),
                    balance.getLeaveTypeId().getName(),
                    balance.getAvailableBalance()
            );
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
