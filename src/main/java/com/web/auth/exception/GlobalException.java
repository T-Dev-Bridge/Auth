package com.web.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public GlobalException(String message, HttpStatus status, String reason) {
        super(message);
        this.status = status;
        this.reason = reason;
    }

}