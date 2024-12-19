package com.web.auth.api.rest;

import com.web.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.base.base.api.ApiResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/no-auth")
public class NoAuthController {

    private final AuthService authService;

    @PostMapping("/encode")
    public ApiResponseDto<String> encode(@RequestBody String rawPassword) {
        return new ApiResponseDto<String>(true, authService.encode(rawPassword));
    }
}
