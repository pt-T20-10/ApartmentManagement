package model;

import java.util.Date;

/**
 * User Entity
 * Represents a user account in the system
 */
public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String role; // ADMIN, USER
    private boolean isActive;
    private Date createdAt;
    private Date lastLogin;
    
    // Constructors
    public User() {
    }
    
    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
    }
    
    public User(Long id, String username, String password, String fullName, 
                String role, boolean isActive, Date createdAt, Date lastLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Helper methods
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
    
    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(this.role);
    }
    
    public boolean isAccountant() {
        return "ACCOUNTANT".equalsIgnoreCase(this.role);
    }
    
    public String getRoleDisplayName() {
        if (isAdmin()) return "Quản trị viên";
        if (isStaff()) return "Nhân viên";
        if (isAccountant()) return "Kế toán";
        return this.role;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}