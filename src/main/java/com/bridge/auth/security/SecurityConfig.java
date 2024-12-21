package com.bridge.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bridge.auth.client.AdminClient;
import com.bridge.auth.security.jwt.JwtAuthenticationFilter;
import com.bridge.auth.security.jwt.JwtAuthorizationFilter;
import com.bridge.auth.security.jwt.JwtTokenProvider;
import com.bridge.auth.service.AuthService;
import com.bridge.auth.service.RefreshTokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static com.bridge.auth.constants.SecurityConstants.BASIC_AUTH_LOGIN_URL;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AdminClient adminClient;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostConstruct
    // SSL 인증서를 무시하는 설정이다. 보안에 취약하며 개발용으로 주로 사용한다.
    private void disableSslVerification(){
        try {
            // 모든 인증서를 신뢰하는 TrustManager를 생성한다.
            // 모든 메서드가 비어있음이 딱히 검증 과정을 거치지 않음을 의미한다.
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // 호스트 네임 검증 비활성화
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Bean
    @Order(1)
    protected SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(authService).passwordEncoder(passwordEncoder);

        // 해당 객체는 메모리 내 인증, LDAP 인증, JDBC 기반 인증을 돕는다.
        // authenticate 메서드를 통해 인증을 수행하고 성공하면 채워진 Authentication 객체를 반환한다.
        // 인증에 실패하면 AuthenticationException 을 던진다.
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors( (cors) -> cors.disable()); // CORS 비활성화
        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.disable()));

        http.authorizeHttpRequests(this::configureAuthorization) // 열어둘 API Endpoint 설정
                        .authenticationManager(authenticationManager)
                        .exceptionHandling(exceptionHandling -> exceptionHandling
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendError(HttpStatus.FORBIDDEN.value());
                                })
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                                })
                        )
                        .sessionManagement((session) -> session
                                // Spring Security가 새션을 생성하거나 사용하지 않는다.
                                // 서버가 클라이언트의 상태 정보를 저장하지 않는다.
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        );
        JwtAuthenticationFilter loginFilter = getAuthenticationFilter(authenticationManager);
        loginFilter.setFilterProcessesUrl(BASIC_AUTH_LOGIN_URL);

        http.addFilter(loginFilter);
        http.addFilter(getAuthorizationFilter(authenticationManager));

        return http.build();
    }
    private JwtAuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        return new JwtAuthenticationFilter(authenticationManager, adminClient, objectMapper, refreshTokenService);
    }
    private JwtAuthorizationFilter getAuthorizationFilter(AuthenticationManager authenticationManager) throws Exception {
        return new JwtAuthorizationFilter(authenticationManager);
    }
    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz){
        authz.requestMatchers(new AntPathRequestMatcher(BASIC_AUTH_LOGIN_URL)).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/no-auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/validate")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/health-check")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/circuitbreaker-health-check")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll();
    }

    public static final boolean isPermitAllPath(String uri) {
        return
                uri.startsWith("/error") ||
                        uri.startsWith("/actuator/") ||
                        uri.startsWith("/h2-console/") ||
                        uri.startsWith("/swagger-ui") ||
                        uri.startsWith("/swagger-resources") ||
                        uri.startsWith("/v3/api-docs") ||
                        uri.startsWith("/v3/api-docs/**") ||
                        uri.startsWith("/health-check") ||
                        uri.startsWith("/api/auth/refresh-token") ||
                        uri.startsWith("/api/no-auth/encode");
    }
}
