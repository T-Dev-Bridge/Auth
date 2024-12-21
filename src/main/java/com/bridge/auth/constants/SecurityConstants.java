package com.bridge.auth.constants;

public class SecurityConstants {

    public static final String BASIC_AUTH_LOGIN_URL = "/api/auth/login";

    public static final String ACCESS_TOKEN_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "refreshtoken";

    public static final String X_MEMBER_HEADER = "X-Member-Header";
    public static final String X_AUTHENTICATED_USERNAME_HEADER = "X-Authenticated-Username";

    public static final String JWT_SECRET = "cdasdsadsadqweqwiohfpoidjnsapfvu809u23pjjiofgDq7mjIUkTMQifCFdOrpg";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "imp-api";
    public static final String TOKEN_AUDIENCE = "imp-app";
    public static final String TOKEN_PREFIX = "Bearer ";
}
