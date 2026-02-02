package dao;

import model.Resident;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Resident operations - UPDATED WITH ID FIX
 */
public class ResidentDAO {
    
    // Get all residents
    public List<Resident> getAllResidents() {
        List<Resident> residents = new ArrayList<>();
        String sql = "SELECT * FROM residents WHERE is_deleted = 0 ORDER BY full_name";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Resident resident = new Resident();
                resident.setId(rs.getLong("id"));
                resident.setFullName(rs.getString("full_name"));
                resident.setPhone(rs.getString("phone"));
                resident.setEmail(rs.getString("email"));
                resident.setIdentityCard(rs.getString("identity_card"));
                resident.setGender(rs.getString("gender"));
                // Fix: Convert java.sql.Date to java.util.Date
                java.sql.Date sqlDate = rs.getDate("dob");
                if (sqlDate != null) {
                    resident.setDob(new java.util.Date(sqlDate.getTime()));
                }
                resident.setHometown(rs.getString("hometown"));
                resident.setDeleted(rs.getBoolean("is_deleted"));
                residents.add(resident);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return residents;
    }
    
    // Get resident by ID
    public Resident getResidentById(Long id) {
        String sql = "SELECT * FROM residents WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Resident resident = new Resident();
                resident.setId(rs.getLong("id"));
                resident.setFullName(rs.getString("full_name"));
                resident.setPhone(rs.getString("phone"));
                resident.setEmail(rs.getString("email"));
                resident.setIdentityCard(rs.getString("identity_card"));
                resident.setGender(rs.getString("gender"));
                // Fix: Convert java.sql.Date to java.util.Date
                java.sql.Date sqlDate = rs.getDate("dob");
                if (sqlDate != null) {
                    resident.setDob(new java.util.Date(sqlDate.getTime()));
                }
                resident.setHometown(rs.getString("hometown"));
                resident.setDeleted(rs.getBoolean("is_deleted"));
                return resident;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Search residents by name (LIKE query)
    public List<Resident> searchResidentsByName(String keyword) {
        List<Resident> residents = new ArrayList<>();
        String sql = "SELECT * FROM residents WHERE full_name LIKE ? AND is_deleted = 0 ORDER BY full_name";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Resident resident = new Resident();
                resident.setId(rs.getLong("id"));
                resident.setFullName(rs.getString("full_name"));
                resident.setPhone(rs.getString("phone"));
                resident.setEmail(rs.getString("email"));
                resident.setIdentityCard(rs.getString("identity_card"));
                resident.setGender(rs.getString("gender"));
                // Fix: Convert java.sql.Date to java.util.Date
                java.sql.Date sqlDate = rs.getDate("dob");
                if (sqlDate != null) {
                    resident.setDob(new java.util.Date(sqlDate.getTime()));
                }
                resident.setHometown(rs.getString("hometown"));
                resident.setDeleted(rs.getBoolean("is_deleted"));
                residents.add(resident);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return residents;
    }
    
    // Insert new resident - FIXED TO SET ID AFTER INSERT
    public boolean insertResident(Resident resident) {
        String sql = "INSERT INTO residents (full_name, phone, email, identity_card, gender, dob, hometown, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, resident.getFullName());
            pstmt.setString(2, resident.getPhone());
            pstmt.setString(3, resident.getEmail());
            pstmt.setString(4, resident.getIdentityCard());
            pstmt.setString(5, resident.getGender());
            if (resident.getDob() != null) {
                pstmt.setDate(6, new java.sql.Date(resident.getDob().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            pstmt.setString(7, resident.getHometown());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // CRITICAL FIX: Get generated ID and set it to the resident object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        resident.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update resident
    public boolean updateResident(Resident resident) {
        String sql = "UPDATE residents SET full_name = ?, phone = ?, email = ?, identity_card = ?, " +
                     "gender = ?, dob = ?, hometown = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, resident.getFullName());
            pstmt.setString(2, resident.getPhone());
            pstmt.setString(3, resident.getEmail());
            pstmt.setString(4, resident.getIdentityCard());
            pstmt.setString(5, resident.getGender());
            if (resident.getDob() != null) {
                pstmt.setDate(6, new java.sql.Date(resident.getDob().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            pstmt.setString(7, resident.getHometown());
            pstmt.setLong(8, resident.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete resident
    public boolean deleteResident(Long id) {
        String sql = "UPDATE residents SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Count total residents
    public int countResidents() {
        String sql = "SELECT COUNT(*) FROM residents WHERE is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // ===== NEW METHODS FOR CONTRACT FORM VALIDATION =====
    
    /**
     * Check if identity card exists (for validation)
     */
    public boolean isIdentityCardExists(String identityCard) {
        String sql = "SELECT COUNT(*) FROM residents WHERE identity_card = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, identityCard);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Check if phone exists (for validation)
     */
    public boolean isPhoneExists(String phone) {
        String sql = "SELECT COUNT(*) FROM residents WHERE phone = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get resident by identity card
     */
    public Resident getResidentByIdentityCard(String identityCard) {
        String sql = "SELECT * FROM residents WHERE identity_card = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, identityCard);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Resident resident = new Resident();
                    resident.setId(rs.getLong("id"));
                    resident.setFullName(rs.getString("full_name"));
                    resident.setPhone(rs.getString("phone"));
                    resident.setEmail(rs.getString("email"));
                    resident.setIdentityCard(rs.getString("identity_card"));
                    resident.setGender(rs.getString("gender"));
                    java.sql.Date sqlDate = rs.getDate("dob");
                    if (sqlDate != null) {
                        resident.setDob(new java.util.Date(sqlDate.getTime()));
                    }
                    resident.setHometown(rs.getString("hometown"));
                    resident.setDeleted(rs.getBoolean("is_deleted"));
                    return resident;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get resident by phone
     */
    public Resident getResidentByPhone(String phone) {
        String sql = "SELECT * FROM residents WHERE phone = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Resident resident = new Resident();
                    resident.setId(rs.getLong("id"));
                    resident.setFullName(rs.getString("full_name"));
                    resident.setPhone(rs.getString("phone"));
                    resident.setEmail(rs.getString("email"));
                    resident.setIdentityCard(rs.getString("identity_card"));
                    resident.setGender(rs.getString("gender"));
                    java.sql.Date sqlDate = rs.getDate("dob");
                    if (sqlDate != null) {
                        resident.setDob(new java.util.Date(sqlDate.getTime()));
                    }
                    resident.setHometown(rs.getString("hometown"));
                    resident.setDeleted(rs.getBoolean("is_deleted"));
                    return resident;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Search residents by keyword (name, phone, identity card)
     */
    public List<Resident> searchResidents(String keyword) {
        List<Resident> residents = new ArrayList<>();
        String sql = "SELECT * FROM residents WHERE " +
                     "(full_name LIKE ? OR phone LIKE ? OR identity_card LIKE ?) " +
                     "AND is_deleted = 0 ORDER BY full_name";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Resident resident = new Resident();
                    resident.setId(rs.getLong("id"));
                    resident.setFullName(rs.getString("full_name"));
                    resident.setPhone(rs.getString("phone"));
                    resident.setEmail(rs.getString("email"));
                    resident.setIdentityCard(rs.getString("identity_card"));
                    resident.setGender(rs.getString("gender"));
                    java.sql.Date sqlDate = rs.getDate("dob");
                    if (sqlDate != null) {
                        resident.setDob(new java.util.Date(sqlDate.getTime()));
                    }
                    resident.setHometown(rs.getString("hometown"));
                    resident.setDeleted(rs.getBoolean("is_deleted"));
                    residents.add(resident);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return residents;
    }
}