package dao;

import model.User;
import connection.Db_connection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for User operations with BCrypt password hashing
 */
public class UserDAO {
    
    /**
     * Authenticate user with BCrypt
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                boolean isPasswordValid;
                
                // Check if password is BCrypt hash or plain text
                if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                    // BCrypt hash - verify using BCrypt
                    isPasswordValid = PasswordUtil.verifyPassword(password, storedPassword);
                    
                    // Check if needs rehashing
                    if (isPasswordValid && PasswordUtil.needsRehash(storedPassword)) {
                        String newHash = PasswordUtil.hashPassword(password);
                        updatePasswordHash(rs.getLong("id"), newHash);
                    }
                } else {
                    // Plain text password - for backward compatibility
                    isPasswordValid = password.equals(storedPassword);
                    
                    // Automatically migrate to BCrypt
                    if (isPasswordValid) {
                        String hashedPassword = PasswordUtil.hashPassword(password);
                        updatePasswordHash(rs.getLong("id"), hashedPassword);
                        System.out.println("✓ Migrated password to BCrypt for user: " + username);
                    }
                }
                
                if (isPasswordValid) {
                    User user = mapResultSetToUser(rs);
                    updateLastLogin(user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Update last login timestamp
     */
    private void updateLastLogin(Long userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update password hash
     */
    private void updatePasswordHash(Long userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert new user (password will be hashed)
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.setBoolean(5, user.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update user information (without changing password)
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, full_name = ?, role = ?, is_active = ? WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, user.isActive());
            pstmt.setLong(5, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Change user password (password will be hashed)
     */
    public boolean changePassword(Long userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            pstmt.setString(1, hashedPassword);
            pstmt.setLong(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Verify current password (for password change validation)
     */
    public boolean verifyCurrentPassword(Long userId, String currentPassword) {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                    return PasswordUtil.verifyPassword(currentPassword, storedPassword);
                } else {
                    return currentPassword.equals(storedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY full_name ASC";
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Delete user (soft delete)
     */
    public boolean deleteUser(Long userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Permanently delete user
     */
    public boolean permanentlyDeleteUser(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username, Long excludeUserId) {
        String sql = excludeUserId != null ? 
            "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?" :
            "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            if (excludeUserId != null) {
                pstmt.setLong(2, excludeUserId);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        return user;
    }
}