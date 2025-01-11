package com.bridge.auth.api.rest;

import com.bridge.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bridge.base.api.CommonResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 필터를 거치지 않을 Endpoint Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/no-auth")
public class NoAuthController {

    private final AuthService authService;

    @PostMapping("/encode")
    public CommonResponseDto<String> encode(@RequestBody String rawPassword) {
        return new CommonResponseDto<String>(true, authService.encode(rawPassword));
    }
}
