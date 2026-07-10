package edu.cit.erag.souvenirpos.auth.dto;

public class LoginResponse {

    private String token;
    private Long userId;
    private String name;
    private String username;
    private String role;

    public LoginResponse(String token, Long userId, String name, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
