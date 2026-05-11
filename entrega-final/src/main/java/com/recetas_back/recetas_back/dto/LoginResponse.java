package com.recetas_back.recetas_back.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private String username;
    private String role;

    public LoginResponse(String token, String username, String role) {
        this(token, null, username, role);
    }

    public LoginResponse(String token, String refreshToken, String username, String role) {
        this.token        = token;
        this.refreshToken = refreshToken;
        this.username     = username;
        this.role         = role;
    }

    public String getToken()        { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getUsername()     { return username; }
    public String getRole()         { return role; }
}
