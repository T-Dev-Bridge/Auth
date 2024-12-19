package com.web.auth.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.web.auth.constants.SecurityConstants;
import com.web.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.base.base.api.ApiResponseDto;
import org.base.base.exception.BackendException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth Controller", description = "인증 컨트롤러")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Validation Check", description = "토큰 유효성 검사")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(SecurityConstants.ACCESS_TOKEN_HEADER) String token,
            HttpServletRequest request
    ) {
        return authService.validateToken(token);
    }

    @Operation(summary = "Refresh Token", description = "토큰 재발급")
    @GetMapping(value="/refresh-token")
    public ApiResponseDto<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refreshToken = request.getHeader(SecurityConstants.REFRESH_TOKEN_HEADER);
        try{
            return new ApiResponseDto<>(authService.refreshToken(refreshToken, response));
        } catch (BackendException | JsonProcessingException e){
            log.error("토큰 갱신 중 오류 발생", e);
            return new ApiResponseDto<>(false, null);
        }
    }
}
