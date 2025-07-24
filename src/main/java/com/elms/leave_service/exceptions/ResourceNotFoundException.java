package com.elms.leave_service.exceptions;

import com.elms.leave_service.enums.ErrorCode;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Object value) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resource + " not found with value: " + value);
    }
}
