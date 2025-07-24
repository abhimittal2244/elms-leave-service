package com.elms.leave_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@Getter
public enum ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource already exists", HttpStatus.CONFLICT),
    INVALID_INPUT("INVALID_INPUT", "Invalid request data", HttpStatus.BAD_REQUEST),
    SQL_ERROR("SQL_ERROR", "Error while communicating with database", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR("INTERNAL_ERROR", "An internal error occurred with server", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    EMPTY_DATA_RECEIVED("EMPTY_DATA_RECEIVED", "Data not provided", HttpStatus.NO_CONTENT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}
