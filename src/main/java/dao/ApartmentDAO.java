package dao;

import model.Apartment;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Apartment operations
 */
public class ApartmentDAO {
    
    // Get all apartments
    public List<Apartment> getAllApartments() {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE is_deleted = 0 ORDER BY room_number";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setId(rs.getLong("id"));
                apartment.setFloorId(rs.getLong("floor_id"));
                apartment.setRoomNumber(rs.getString("room_number"));
                apartment.setArea(rs.getDouble("area"));
                apartment.setStatus(rs.getString("status"));
                apartment.setBasePrice(rs.getBigDecimal("base_price"));
                apartment.setDescription(rs.getString("description"));
                apartment.setDeleted(rs.getBoolean("is_deleted"));
                apartments.add(apartment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }
    
    // Get apartments by floor ID
    public List<Apartment> getApartmentsByFloorId(Long floorId) {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE floor_id = ? AND is_deleted = 0 ORDER BY room_number";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, floorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setId(rs.getLong("id"));
                apartment.setFloorId(rs.getLong("floor_id"));
                apartment.setRoomNumber(rs.getString("room_number"));
                apartment.setArea(rs.getDouble("area"));
                apartment.setStatus(rs.getString("status"));
                apartment.setBasePrice(rs.getBigDecimal("base_price"));
                apartment.setDescription(rs.getString("description"));
                apartment.setDeleted(rs.getBoolean("is_deleted"));
                apartments.add(apartment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }
    
    // ===== ADDED FOR PANEL COMPATIBILITY =====
    
    /**
     * Get apartments by building ID (through floor relationship)
     */
    public List<Apartment> getApartmentsByBuildingId(Long buildingId) {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT a.* FROM apartments a " +
                     "JOIN floors f ON a.floor_id = f.id " +
                     "WHERE f.building_id = ? AND a.is_deleted = 0 " +
                     "ORDER BY a.room_number";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, buildingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setId(rs.getLong("id"));
                apartment.setFloorId(rs.getLong("floor_id"));
                apartment.setRoomNumber(rs.getString("room_number"));
                apartment.setArea(rs.getDouble("area"));
                apartment.setStatus(rs.getString("status"));
                apartment.setBasePrice(rs.getBigDecimal("base_price"));
                apartment.setDescription(rs.getString("description"));
                apartment.setDeleted(rs.getBoolean("is_deleted"));
                apartments.add(apartment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }
    
    /**
     * Count apartments by status
     */
    public int countApartmentsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Get apartment by ID
    public Apartment getApartmentById(Long id) {
        String sql = "SELECT * FROM apartments WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setId(rs.getLong("id"));
                apartment.setFloorId(rs.getLong("floor_id"));
                apartment.setRoomNumber(rs.getString("room_number"));
                apartment.setArea(rs.getDouble("area"));
                apartment.setStatus(rs.getString("status"));
                apartment.setBasePrice(rs.getBigDecimal("base_price"));
                apartment.setDescription(rs.getString("description"));
                apartment.setDeleted(rs.getBoolean("is_deleted"));
                return apartment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insert new apartment
    public boolean insertApartment(Apartment apartment) {
        String sql = "INSERT INTO apartments (floor_id, room_number, area, status, base_price, description, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, apartment.getFloorId());
            pstmt.setString(2, apartment.getRoomNumber());
            pstmt.setDouble(3, apartment.getArea());
            pstmt.setString(4, apartment.getStatus());
            pstmt.setBigDecimal(5, apartment.getBasePrice());
            pstmt.setString(6, apartment.getDescription());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update apartment
    public boolean updateApartment(Apartment apartment) {
        String sql = "UPDATE apartments SET floor_id = ?, room_number = ?, area = ?, status = ?, " +
                     "base_price = ?, description = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, apartment.getFloorId());
            pstmt.setString(2, apartment.getRoomNumber());
            pstmt.setDouble(3, apartment.getArea());
            pstmt.setString(4, apartment.getStatus());
            pstmt.setBigDecimal(5, apartment.getBasePrice());
            pstmt.setString(6, apartment.getDescription());
            pstmt.setLong(7, apartment.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete apartment
    public boolean deleteApartment(Long id) {
        String sql = "UPDATE apartments SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Count total apartments
    public int countApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE is_deleted = 0";
        
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
    
    // Count available apartments
    public int countAvailableApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = 'AVAILABLE' AND is_deleted = 0";
        
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
    
    // Count rented apartments
    public int countRentedApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = 'RENTED' AND is_deleted = 0";
        
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
}