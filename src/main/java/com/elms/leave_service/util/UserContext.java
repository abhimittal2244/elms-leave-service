package com.elms.leave_service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserContext {

    public String getCurrentUserId() {
        return getRequest().getHeader("X-User-Id");
    }

    public String getCurrentUserRole() {
        return getRequest().getHeader("X-User-Roles");
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attr != null;
        return attr.getRequest();
    }
}
