package com.elms.leave_service.service;

import com.elms.leave_service.enums.ErrorCode;
import com.elms.leave_service.exceptions.ApiException;
import com.elms.leave_service.exceptions.DuplicateResourceException;
import com.elms.leave_service.exceptions.ResourceNotFoundException;
import com.elms.leave_service.model.LeaveType;
import com.elms.leave_service.modelDto.LeaveTypeRequestDto;
import com.elms.leave_service.modelDto.LeaveTypeResponseDto;
import com.elms.leave_service.repository.LeaveTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveTypeService {
    @Autowired
    LeaveTypeRepository leaveTypeRepository;

    @Autowired
    LeaveBalanceService leaveBalanceService;

    @Transactional
    public LeaveTypeResponseDto createLeaveType(LeaveTypeRequestDto dto) {
        try{
            if(leaveTypeRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
                    throw new DuplicateResourceException("Leave Type", dto.getName());
            }

            if(dto.isRollable() && dto.getMaxCarryForward() > dto.getMaxDays()) {
                throw new ApiException(ErrorCode.INVALID_INPUT, "Max Carry Forward cannot be greater than max allowed days.");
            }

            LeaveType leaveType = new LeaveType();
            leaveType.setName(dto.getName());
            leaveType.setMaxDays(dto.getMaxDays());
            leaveType.setRollable(dto.isRollable());
            leaveType.setMaxCarryForward(dto.isRollable() ? dto.getMaxCarryForward() : 0);

            LeaveType saved = leaveTypeRepository.save(leaveType);

            return leaveBalanceService.initializeAllEmployeesLeaveBalanceByLeaveType(saved);

        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    public List<LeaveTypeResponseDto> getAllLeaveTypes() {
        List<LeaveType> leaveTypes =  leaveTypeRepository.findAll();
        return leaveTypes.stream().map(type -> new LeaveTypeResponseDto(
                type.getName(),
                type.getMaxDays(),
                type.isRollable(),
                type.getMaxCarryForward()
        )).collect(Collectors.toList());
    }

    @Transactional
    public LeaveTypeResponseDto updateLeaveType(int id, LeaveTypeRequestDto dto) {
        try{
            LeaveType leaveType = leaveTypeRepository.findById(id).orElseThrow(() ->
                    new ResourceNotFoundException("Leave Type", id));

            LeaveType existingByName = leaveTypeRepository.findByNameIgnoreCase(dto.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Leave Type",dto.getName()));

            if(existingByName.getId() != id)
                throw new DuplicateResourceException("Leave Type", dto.getName());


            if(dto.isRollable() && dto.getMaxCarryForward() > dto.getMaxDays()) {
                throw new ApiException(ErrorCode.INVALID_INPUT, "Max Carry Forward cannot be greater than max allowed days.");
            }

            leaveType.setName(dto.getName());
            leaveType.setMaxDays(dto.getMaxDays());
            leaveType.setRollable(dto.isRollable());
            leaveType.setMaxCarryForward(dto.isRollable() ? dto.getMaxCarryForward() : 0);
            LeaveType saved = leaveTypeRepository.save(leaveType);
            return new LeaveTypeResponseDto(
                    saved.getName(),
                    saved.getMaxDays(),
                    saved.isRollable(),
                    saved.getMaxCarryForward()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SQL_ERROR, e.getMessage());
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, ex.getCause());
        }
    }
}
