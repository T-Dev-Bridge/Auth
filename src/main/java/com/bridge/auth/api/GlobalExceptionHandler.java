package com.bridge.auth.api;

import com.bridge.auth.exception.GlobalException;
import org.bridge.base.api.CommonResponseDto;
import org.bridge.base.exception.CommonException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GlobalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> handleAllExceptions(GlobalException ex, WebRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : ex.getReason());
        body.put("path", request.getDescription(false).substring(4)); // Strip "uri=" prefix

        CommonResponseDto<Map<String, Object>> response = new CommonResponseDto<>(false, body);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(CommonException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> handleAllExceptions(CommonException ex, WebRequest request) {
        HttpStatus status = ex.getErrorCode() != null ? HttpStatus.valueOf(ex.getErrorCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", ex.getCause());
        body.put("message", ex.getMessage() != null ? ex.getMessage() : ex.getReason());
        body.put("path", request.getDescription(false).substring(4)); // Strip "uri=" prefix

        CommonResponseDto<Map<String, Object>> response = new CommonResponseDto<>(false, body);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> handleNotFound(NoHandlerFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", "The requested resource was not found");
        body.put("path", request.getDescription(false).substring(4));

        CommonResponseDto<Map<String, Object>> response = new CommonResponseDto<>(false, body);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        CommonResponseDto<Map<String, Object>> response = new CommonResponseDto<>(false, body);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
