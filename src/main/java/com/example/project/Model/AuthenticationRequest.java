package com.example.project.Model;

public class AuthenticationRequest {
    private String username;
    private String password;

    // Default constructor (required for deserialization)
    public AuthenticationRequest() {
    }

    // Getters and setters for username and password
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
