package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    public User(int userId, String username, String fullName, String role, String status, 
                LocalDateTime lastLogin, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        
        return (this.fullName != null && !this.fullName.isEmpty()) ? this.fullName : this.username;
    }
}