package com.elms.leave_service.feignClient;

import com.elms.leave_service.config.FeignConfig;
import com.elms.leave_service.ClientDto.EmployeeMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient("EMPLOYEE-SERVICE")
@FeignClient(name="EMPLOYEE-SERVICE",
configuration = FeignConfig.class)
public interface EmployeeClient {
    @GetMapping("/employee/employees/getEmployeeMetadata")
    public ResponseEntity<EmployeeMetadataDto> getEmployeeMetadata();

    @GetMapping("/employee/employees/getAllApprovedEmployees")
    public ResponseEntity<List<EmployeeMetadataDto>> getAllApprovedEmployees();

    @GetMapping("/employee/employees/manager/{managerId}/employees")
    public ResponseEntity<List<EmployeeMetadataDto>> getTeamMembers(@PathVariable int managerId);

    @GetMapping("/employee/employees/name/{id}")
    public ResponseEntity<String> getEmployeeNameById(@PathVariable int id);

    @GetMapping("/employee/employees/admin/employees")
    public ResponseEntity<List<EmployeeMetadataDto>> getAllAdminTeamMembers();

}
