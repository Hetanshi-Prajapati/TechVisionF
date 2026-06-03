package com.example.signup.dto;

import com.example.signup.entity.User;

public class LoginResponse {

    private String token;
    private Long id;
    private String username;
    private String email;
    private String fullName;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
