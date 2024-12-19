package com.web.auth.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.auth.client.AdminClient;
import com.web.auth.constants.SecurityConstants;
import com.web.auth.security.UserPrincipal;
import com.web.auth.service.Core.ManagerDto;
import com.web.auth.service.CustomManagerDetails;
import com.web.auth.service.RefreshTokenDto;
import com.web.auth.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.base.base.api.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final AdminClient adminClient;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserPrincipal userPrincipal = new ObjectMapper().readValue(request.getInputStream(), UserPrincipal.class);
            String username = userPrincipal.getUsername();
            String password = userPrincipal.getPassword();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

            try {
                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                boolean isAuthenticated = authentication.isAuthenticated();
                log.info("username : {}, isAuthenticated : {}", username, isAuthenticated);

                return authentication;

            } catch (AuthenticationException e) {
                handleAuthenticationException(response, username, e);
            }
            return null;
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void handleAuthenticationException(HttpServletResponse response,
                                               String username, AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponseDto errorResponse = new ApiResponseDto();
        errorResponse.setSuccess(false);

        if (e instanceof BadCredentialsException | e instanceof InternalAuthenticationServiceException) {
            ApiResponseDto<String> result = coreClient.getManagerIdById(username);
            if(result.isSuccess()) {
                errorResponse.setMessage("비밀번호가 일치하지 않습니다.");
            } else {
                errorResponse.setMessage("존재하지 않는 사용자입니다.");
            }
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
            response.getWriter().close();
        } else if (e instanceof DisabledException){
            errorResponse.setMessage("비활성화된 계정입니다.");
            response.setStatus(HttpStatus.LOCKED.value());

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    @Override
    // 인증 성공시 처리
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain, Authentication authentication) throws IOException {
        String userId;
        CustomManagerDetails manager = (CustomManagerDetails) authentication.getPrincipal();
        userId = manager.getUserId();
        processAuthenticationSuccess(authentication.getPrincipal(), userId, manager.getUsername(),
                manager.getAuthorities(), response, authentication);
    }

    private void processAuthenticationSuccess(Object principal, String userId, String username,
                                              Collection<? extends GrantedAuthority> authorities, HttpServletResponse response,
                                              Authentication authentication) throws IOException {

        // accessToken 발급
        String accessToken = JwtTokenProvider.doGenerateAccessToken(
                userId, username,
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        // refreshToken 발급
        String refreshToken = JwtTokenProvider.doGenerateRefreshToken(
                userId, username,
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        // refresh token 적재
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken(refreshToken);
        refreshTokenDto.setUserId(userId);


        if (this.refreshTokenService.getById(username) != null)
            this.refreshTokenService.deleteById(username);
        this.refreshTokenService.save(refreshTokenDto, userId);

        response.setHeader(SecurityConstants.ACCESS_TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + accessToken);
        response.setHeader(SecurityConstants.REFRESH_TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + refreshToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ApiResponseDto responseDto = new ApiResponseDto(authentication.isAuthenticated(), principal);

        new ObjectMapper().writeValue(response.getWriter(), responseDto);
    }
}
