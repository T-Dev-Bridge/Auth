package com.web.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.auth.client.AdminClient;
import com.web.auth.constants.SecurityConstants;
import com.web.auth.security.jwt.JwtTokenProvider;
import com.web.auth.service.Core.ManagerDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.base.base.api.ApiResponseDto;
import org.base.base.exception.BackendException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    private final AdminClient adminClient;

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {

            ManagerDto dto  = adminClient.getManagerById(username).getData();
            if(dto==null){
                throw new UsernameNotFoundException("User not found : " + username);
            }

            User.UserBuilder builder = User.withUsername(dto.getUsername());
            builder.username(dto.getUsername());
            builder.password(dto.getPassword());
            builder.roles(String.valueOf(dto.getRoleId()));
            builder.disabled(!dto.isEnabled());
            return new CustomManagerDetails(builder.build(), dto);
        } catch (NoSuchElementException e) {
            log.error("Username(id) not found. username(id) : {}", username);
            throw new UsernameNotFoundException("User not found");
        }
    }

    public ResponseEntity<String> validateToken(String token){
        try {
            Map<String, String> authInfo = jwtTokenProvider.getAuthInfoByToken(token);
            log.info("authInfo : {}", authInfo.get("userId"));
            if(authInfo == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }
            return ResponseEntity.ok()
                    .header(SecurityConstants.X_AUTHENTICATED_USERNAME_HEADER, authInfo.get("userId"))
                    .body("Valid Token");
        } catch (RuntimeException e){
            log.error("Token validation failed. token : {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
    }

    public boolean refreshToken(String refreshToken, HttpServletResponse response) throws BackendException, JsonProcessingException{
        if (refreshToken == null){
            log.error("refreshToken, Refresh token not found");
            return false;
        }
        refreshToken = refreshToken.replace(SecurityConstants.TOKEN_PREFIX, "");
        RefreshTokenDto refreshTokenDto = refreshTokenService.getByToken(refreshToken);

        if(refreshTokenDto == null){
            log.error("refreshToken, Refresh token not found");
            return false;
        }

        return processRefreshToken(refreshTokenDto, response);
    }


    private boolean processRefreshToken(RefreshTokenDto refreshTokenDto, HttpServletResponse response) throws JsonProcessingException{
        ApiResponseDto apiResponseDto = this.adminClient.getManagerById(refreshTokenDto.getUserId());
        String jsonString = objectMapper.writeValueAsString(apiResponseDto.getData());
        ManagerDto managerDto = objectMapper.readValue(jsonString, ManagerDto.class);

        if (managerDto != null){
            return generateAndSaveTokens(managerDto.getId(), managerDto.getUsername(), managerDto.getRoleId(), refreshTokenDto, response);
        }else {
            log.error("refreshToken, User not found");
            return false;
        }
    }

    private boolean generateAndSaveTokens(String userId, String username, long roleId, RefreshTokenDto refreshTokenDto,
                                          HttpServletResponse response){
        String accessToken = JwtTokenProvider.doGenerateAccessToken(
                userId,
                username,
                List.of(String.valueOf(roleId)));
        log.info("refreshToken, Create access token by refresh token : {}, {}, ", userId, accessToken);

        // refresh token 갱신
        String refreshToken = JwtTokenProvider.doGenerateRefreshToken(
                userId,
                username,
                List.of(String.valueOf(roleId)));
        log.info("refreshToken, Create refresh token by refresh token : {}, {}, ", userId, refreshToken);

        // refresh token 적재
        refreshTokenDto.setRefreshToken(refreshToken);
        refreshTokenService.save(refreshTokenDto, userId);

        JwtTokenProvider.setTokenMap(userId, SecurityConstants.TOKEN_PREFIX + accessToken);

        response.setHeader(SecurityConstants.ACCESS_TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + accessToken);
        response.setHeader(SecurityConstants.REFRESH_TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + refreshToken);

        return true;
    }
}
