package com.elms.leave_service.ClientDto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Data
@Setter
@Getter
@AllArgsConstructor
@ToString
public class EmployeeMetadataDto {
    private int employeeId;
    private String fullName;
    private String email;
    private LocalDate joinDate;
    private String phone;
    private String role;
    private int managerId;
    private int designationId;
    private int departmentId;
}
