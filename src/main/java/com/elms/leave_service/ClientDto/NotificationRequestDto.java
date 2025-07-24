package com.elms.leave_service.ClientDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationRequestDto {
    private int receiverId;
    private String title;
    private String message;
}
