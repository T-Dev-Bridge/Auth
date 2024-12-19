package com.web.auth.security.jwt;

import com.web.auth.constants.SecurityConstants;
import com.web.auth.security.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
            , FilterChain filterChain)throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        log.info(String.format("########### JwtAuthorizationFilter doFilter [%s]", requestURI));
        if(SecurityConfig.isPermitAllPath(requestURI)) { // 필터링이 필요하지 않은 경로인지 확인
            filterChain.doFilter(request, response);
            return;
        }

        try{
            // 인증 로직
            UsernamePasswordAuthenticationToken authentication = getAuthentication(request, response);
            if(authentication == null) {
                filterChain.doFilter(request, response);
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            // 요청 속성에 예외 저장
            request.setAttribute("exception", e);
            // 예외 전파
            throw e;
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 헤더에서 토큰을 가져온다.
        String token = request.getHeader(SecurityConstants.ACCESS_TOKEN_HEADER);

        return token != null ? JwtTokenProvider.getAuthentication(token, response) : null;
    }
}
