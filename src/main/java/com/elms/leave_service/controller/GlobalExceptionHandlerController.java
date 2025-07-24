package com.elms.leave_service.controller;

import com.elms.leave_service.enums.ErrorCode;
import com.elms.leave_service.exceptions.*;
import com.elms.leave_service.modelDto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

//    @ExceptionHandler(LeaveRequestNotFoundException.class)
//    public ResponseEntity<?> handleLeaveRequestNotFoundException(LeaveRequestNotFoundException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(LeaveAuditException.class)
//    public ResponseEntity<?> handleLeaveAuditException(LeaveAuditException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(LeaveRequestException.class)
//    public ResponseEntity<?> handleLeaveRequestException(LeaveRequestException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_ACCEPTABLE);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGenericException(Exception ex) {
//        return buildErrorResponse("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(LeaveBalanceException.class)
//    public ResponseEntity<?> handleLeaveBalanceException(LeaveBalanceException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(LeaveTypeNotFoundException.class)
//    public ResponseEntity<?> handleLeaveTypeNotFoundException(LeaveTypeNotFoundException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(LeaveTypeException.class)
//    public ResponseEntity<?> handleLeaveTypeException(LeaveTypeException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_ACCEPTABLE);
//    }
//
//    private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", status.value());
//        body.put("error", status.getReasonPhrase());
//        body.put("message", message);
//        return new ResponseEntity<>(body, status);
//    }
}