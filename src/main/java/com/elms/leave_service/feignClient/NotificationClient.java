package com.elms.leave_service.feignClient;

import com.elms.leave_service.ClientDto.NotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="NOTIFICATION-SERVICE")
public interface NotificationClient {
    @PostMapping("notification/notifications/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequestDto request);
}
