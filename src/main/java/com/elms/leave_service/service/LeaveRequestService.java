package com.elms.leave_service.service;

import com.elms.leave_service.ClientDto.EmployeeMetadataDto;
import com.elms.leave_service.ClientDto.NotificationRequestDto;
import com.elms.leave_service.enums.DayType;
import com.elms.leave_service.enums.ErrorCode;
import com.elms.leave_service.enums.LeaveStatus;
import com.elms.leave_service.exceptions.ApiException;
import com.elms.leave_service.exceptions.ResourceNotFoundException;
import com.elms.leave_service.feignClient.EmployeeClient;
import com.elms.leave_service.feignClient.NotificationClient;
import com.elms.leave_service.model.LeaveBalance;
import com.elms.leave_service.model.LeaveRequest;
import com.elms.leave_service.model.LeaveType;
import com.elms.leave_service.modelDto.LeaveDecisionDto;
import com.elms.leave_service.modelDto.LeaveRequestDto;
import com.elms.leave_service.modelDto.LeaveResponseDto;
import com.elms.leave_service.repository.LeaveBalanceRepository;
import com.elms.leave_service.repository.LeaveRequestRepository;
import com.elms.leave_service.repository.LeaveTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private EmployeeClient employeeClient;

    @Autowired
    private LeaveAuditService leaveAuditService;

    @Autowired
    private NotificationClient notificationClient;

    @Transactional
    public LeaveResponseDto applyLeave(LeaveRequestDto dto) {
        try{
            EmployeeMetadataDto employee = employeeClient.getEmployeeMetadata().getBody();
            if (employee == null) {
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Employee metadata not found");
            }

            boolean isOverlapping = leaveRequestRepository.existsByEmployeeIdAndLeaveStatusInAndDateRangeOverlap(
                    employee.getEmployeeId(),
                    List.of(LeaveStatus.APPROVED, LeaveStatus.PENDING),
                    dto.getStartDate(),
                    dto.getEndDate()
            );

            if (isOverlapping) {
                throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Leave already exists in the selected date range.");
            }

            if(dto.getStartDate().isAfter(dto.getEndDate()))
                throw new ApiException(ErrorCode.INVALID_INPUT, "Leave end date can not be before Leave Start Date");

            LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveType())
                    .orElseThrow(() -> new ResourceNotFoundException("Leave Type", dto.getLeaveType()));

            double leaveCount = calculateLeaveDays(dto.getStartDate(), dto.getEndDate(), dto.getDayType());

            int year = LocalDate.now().getYear();

            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    employee.getEmployeeId(),
                    leaveType,
                    year)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Leave Balance Not Found"));
            if (balance.getAvailableBalance() < leaveCount) {
                throw new ApiException(ErrorCode.INVALID_INPUT, "Insufficient Leave Balance");
            }

            LeaveRequest request = new LeaveRequest();
            request.setEmployeeId(employee.getEmployeeId());
            request.setLeaveType(leaveType);
            request.setStartDate((dto.getStartDate()));
            request.setEndDate(dto.getEndDate());
            request.setDayType(dto.getDayType());
            request.setReason(dto.getReason());
            request.setLeaveStatus(LeaveStatus.PENDING);
            request.setAppliedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());


            LeaveRequest saved = leaveRequestRepository.save(request);

            leaveAuditService.addAuditLog(
                    saved,
                    saved.getLeaveStatus(),
                    saved.getReason(),
                    saved.getEmployeeId(),
                    saved.getAppliedAt()
            );

            String msg = notificationClient.sendNotification(
                    new NotificationRequestDto(
                            employee.getManagerId() == 0 ? employee.getEmployeeId() : employee.getManagerId(),
                            "New Leave Request",
                            employee.getFullName()
                                    + " has applied for leave from "
                                    + saved.getStartDate() + " to " + saved.getEndDate()
                    )
            ).getBody();

            return new LeaveResponseDto(
                    saved.getId(),
                    saved.getEmployeeId(),
                    employeeClient.getEmployeeNameById(saved.getEmployeeId()).getBody(),
                    saved.getLeaveType().getName(),
                    saved.getStartDate(),
                    saved.getEndDate(),
                    saved.getDayType().toString(),
                    saved.getReason(),
                    saved.getLeaveStatus().toString(),
                    saved.getAppliedAt(),
                    saved.getUpdatedAt(),
                    "None"
            );
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public List<LeaveResponseDto> getLeaveRequestsByEmployee() {
        try {
            EmployeeMetadataDto employee = employeeClient.getEmployeeMetadata().getBody();
            assert employee != null;
            List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getEmployeeId());
            return requests.stream().map(request -> new LeaveResponseDto(
                    request.getId(),
                    request.getEmployeeId(),
                    employee.getFullName(),
                    request.getLeaveType().getName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDayType().toString(),
                    request.getReason(),
                    request.getLeaveStatus().toString(),
                    request.getAppliedAt(),
                    request.getUpdatedAt(),
                    request.getApprovedBy() != 0 ?
                            employeeClient.getEmployeeNameById(request.getApprovedBy()).getBody() : "None"
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public List<LeaveResponseDto> getLeaveRequestsUnderManager() {
        try {
            EmployeeMetadataDto manager = employeeClient.getEmployeeMetadata().getBody();
            assert manager != null;

            List<EmployeeMetadataDto> teamMembers = employeeClient.getTeamMembers(manager.getEmployeeId()).getBody();
            if (teamMembers == null) {
                return Collections.emptyList();
            }

            List<Integer> employeeIds = teamMembers.stream()
                    .map(EmployeeMetadataDto::getEmployeeId)
                    .toList();
            List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeIdIn(employeeIds);

            return requests.stream().map(request -> {
                int employeeId = request.getEmployeeId();
                if (employeeId == 0) {
                    throw new ApiException(ErrorCode.INVALID_INPUT, "Parsed invalid employee Id from Leave Request");
                }

                String employeeName = employeeClient.getEmployeeNameById(employeeId).getBody();

                String updatedByName = "";
                int approvedById = request.getApprovedBy();
                if (approvedById != 0) {
                    updatedByName = employeeClient.getEmployeeNameById(approvedById).getBody();
                    if (updatedByName == null) {
                        updatedByName = "";
                    }
                }
                return new LeaveResponseDto(
                        request.getId(),
                        employeeId,
                        employeeName,
                        request.getLeaveType().getName(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getDayType().toString(),
                        request.getReason(),
                        request.getLeaveStatus().toString(),
                        request.getAppliedAt(),
                        request.getUpdatedAt(),
                        updatedByName
                );
            }).collect(Collectors.toList()
            );
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INVALID_INPUT, ex.getMessage());
        }
    }

    @Transactional
    public String updateLeaveStatus(int leaveId, LeaveDecisionDto dto) {
        try{
            int approverId = Objects.requireNonNull(employeeClient.getEmployeeMetadata().getBody()).getEmployeeId();
            LeaveRequest request = leaveRequestRepository.findById(leaveId)
                    .orElseThrow(() -> new ResourceNotFoundException("Leave Request Id",leaveId));
            if(request.getLeaveStatus() != LeaveStatus.PENDING) {
                throw new ApiException(ErrorCode.INVALID_INPUT);
            }
            request.setLeaveStatus(dto.getStatus());
            if(dto.getStatus()==LeaveStatus.APPROVED)
                updateLeaveBalance(request);
            request.setManagerComment(dto.getManagerComment());
            request.setApprovedBy(approverId);
            request.setUpdatedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);

            leaveAuditService.addAuditLog(
                    request,
                    request.getLeaveStatus(),
                    request.getManagerComment(),
                    request.getApprovedBy(),
                    request.getUpdatedAt()
            );

            String msg = notificationClient.sendNotification(
                    new NotificationRequestDto(
                            request.getEmployeeId(),
                            " Leave "+request.getLeaveStatus().toString(),
                            "Your leave request has been " + request.getLeaveStatus().name().toLowerCase()
                                    + ((request.getManagerComment() != null) ? ("/nManager Comments: "+request.getManagerComment()) : "")
                    )
            ).getBody();

            System.out.println("Sent Message: "+msg);

            return "Leave status update to: "+request.getLeaveStatus();
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Failed to update leave status : "+e.getMessage());
        }
    }

    @Transactional
    public String overrideLeaveRequest(int leaveRequestId, LeaveDecisionDto dto) {
        try{
            LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                    .orElseThrow(() -> new RuntimeException("LeaveRequestId not found"));
            int approverId = Objects.requireNonNull(employeeClient.getEmployeeMetadata().getBody()).getEmployeeId();
            if(request.getLeaveStatus() == LeaveStatus.PENDING) {
                request.setLeaveStatus(dto.getStatus());
                if(dto.getStatus()==LeaveStatus.APPROVED)
                    updateLeaveBalance(request);

//                throw new ApiException(ErrorCode.INVALID_INPUT, "Pending Leaves can not be overridden!!");
            }
            else if(request.getLeaveStatus() == LeaveStatus.REJECTED) {
                request.setLeaveStatus(LeaveStatus.APPROVED);
                updateLeaveBalance(request);
            }
            else
            {
                request.setLeaveStatus(LeaveStatus.REJECTED);
                double leaveCount = calculateLeaveDays(
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getDayType()
                );

                LeaveBalance balance = leaveBalanceRepository
                        .findByEmployeeIdAndLeaveTypeIdAndYear(
                                request.getEmployeeId(),
                                request.getLeaveType(),
                                request.getStartDate().getYear()
                        ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Leave balance not found"));

                balance.setAvailableBalance(balance.getAvailableBalance() + leaveCount);
            }

            request.setManagerComment(dto.getManagerComment());
            request.setApprovedBy(approverId);
            request.setUpdatedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);

            leaveAuditService.addAuditLog(
                    request,
                    request.getLeaveStatus(),
                    request.getManagerComment(),
                    request.getApprovedBy(),
                    request.getUpdatedAt()
            );

            String msg = notificationClient.sendNotification(
                    new NotificationRequestDto(
                            request.getEmployeeId(),
                            " Leave Overridden to "+request.getLeaveStatus().toString(),
                            "Your leave request has been " + request.getLeaveStatus().name().toLowerCase()
                                    + ((request.getManagerComment() != null) ? ("/nManager Comments: "+request.getManagerComment()) : "")
                    )
            ).getBody();


            return "Leave Request update to " + request.getLeaveStatus();
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to override leave request : "+e.getMessage());
        }
    }




    @Transactional
    private void updateLeaveBalance(LeaveRequest request) {
        try{
            double leaveCount = calculateLeaveDays(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDayType()
            );

            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(
                            request.getEmployeeId(),
                            request.getLeaveType(),
                            request.getStartDate().getYear()
                    ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Leave balance not found"));

            if(request.getLeaveStatus() == LeaveStatus.APPROVED) {
                if(balance.getAvailableBalance() < leaveCount) {
                    throw new ApiException(ErrorCode.INVALID_INPUT, "Insufficient leave balance at approval time");
                }

                balance.setAvailableBalance(balance.getAvailableBalance() - leaveCount);

                leaveBalanceRepository.save(balance);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to update leave balance: "+e.getMessage());
        }
    }

    public double calculateLeaveDays(LocalDate start, LocalDate end, DayType dayType) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if(days>1 && dayType==DayType.HALF_DAY)
            throw new ApiException(ErrorCode.INVALID_INPUT, "HALF_DAY leave can not be applied for more than 1 day");
        return dayType == DayType.FULL_DAY ? days : 0.5;
    }

    public List<LeaveResponseDto> getAllAdminLeaveRequests() {
        try {

            EmployeeMetadataDto employee = employeeClient.getEmployeeMetadata().getBody();
            List<EmployeeMetadataDto> teamMembers = employeeClient.getAllAdminTeamMembers().getBody();

            if(teamMembers != null) {

                List<Integer> employeeIds = new java.util.ArrayList<>(teamMembers.stream()
                        .map(EmployeeMetadataDto::getEmployeeId)
                        .toList());
                assert employee != null;
                employeeIds.add(employee.getEmployeeId());

                List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeIdIn(employeeIds);

    //            return requests.stream().map(request -> new LeaveResponseDto(
    //                    request.getId(),
    //                    request.getEmployeeId(),
    //                    Optional<String> employeeName = teamMembers.stream()
    //                            .filter(emp -> emp.getEmployeeId() == 1)
    //                            .map(EmployeeMetadataDto::getFullName)
    //                            .findFirst();
    //                    employee.getFullName(),
    //                    request.getLeaveType().getName(),
    //                    request.getStartDate(),
    //                    request.getEndDate(),
    //                    request.getDayType().toString(),
    //                    request.getReason(),
    //                    request.getLeaveStatus().toString(),
    //                    request.getAppliedAt(),
    //                    request.getUpdatedAt(),
    //                    request.getApprovedBy() == 0 ? "None" :
    //                    employeeClient.getEmployeeNameById(request.getApprovedBy()).getBody()
    //            )).collect(Collectors.toList());

                return requests.stream()
                        .map(request -> {
                            // Find employeeName for this request's employeeId from teamMembers list
                            String employeeName = teamMembers.stream()
                                    .filter(emp -> emp.getEmployeeId() == request.getEmployeeId())
                                    .map(EmployeeMetadataDto::getFullName)
                                    .findFirst()
                                    .orElse(employee.getFullName());

                            String approvedByName = request.getApprovedBy() == 0
                                    ? "None"
                                    : employeeClient.getEmployeeNameById(request.getApprovedBy()).getBody();

                            return new LeaveResponseDto(
                                    request.getId(),
                                    request.getEmployeeId(),
                                    employeeName,
                                    request.getLeaveType().getName(),
                                    request.getStartDate(),
                                    request.getEndDate(),
                                    request.getDayType().toString(),
                                    request.getReason(),
                                    request.getLeaveStatus().toString(),
                                    request.getAppliedAt(),
                                    request.getUpdatedAt(),
                                    approvedByName
                            );
                        })
                        .collect(Collectors.toList());

            }
            else {
                return Collections.emptyList();
            }
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }
}
