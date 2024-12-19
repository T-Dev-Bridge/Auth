package com.web.auth.security;

import lombok.Data;

@Data
public class UserPrincipal {

    private String userId;
    private String username;
    private String password;
    private String[] roles;
    private String pushToken;

    public UserPrincipal() {
    }
    public UserPrincipal(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }
    public UserPrincipal(String userId, String username, String password, String pushToken, String... roles) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.pushToken = pushToken;
        this.roles = roles;
    }

}
