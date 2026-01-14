package dao;

import model.Floor;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Floor operations
 */
public class FloorDAO {
    
    // Get all floors
    public List<Floor> getAllFloors() {
        List<Floor> floors = new ArrayList<>();
        String sql = "SELECT * FROM floors WHERE is_deleted = 0 ORDER BY floor_number";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Floor floor = new Floor();
                floor.setId(rs.getLong("id"));
                floor.setBuildingId(rs.getLong("building_id"));
                floor.setFloorNumber(rs.getInt("floor_number"));
                floor.setName(rs.getString("name"));
                floor.setDeleted(rs.getBoolean("is_deleted"));
                floors.add(floor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return floors;
    }
    
    // Get floors by building ID
    public List<Floor> getFloorsByBuildingId(Long buildingId) {
        List<Floor> floors = new ArrayList<>();
        String sql = "SELECT * FROM floors WHERE building_id = ? AND is_deleted = 0 ORDER BY floor_number";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, buildingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Floor floor = new Floor();
                floor.setId(rs.getLong("id"));
                floor.setBuildingId(rs.getLong("building_id"));
                floor.setFloorNumber(rs.getInt("floor_number"));
                floor.setName(rs.getString("name"));
                floor.setDeleted(rs.getBoolean("is_deleted"));
                floors.add(floor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return floors;
    }
    
    // Get floor by ID
    public Floor getFloorById(Long id) {
        String sql = "SELECT * FROM floors WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Floor floor = new Floor();
                floor.setId(rs.getLong("id"));
                floor.setBuildingId(rs.getLong("building_id"));
                floor.setFloorNumber(rs.getInt("floor_number"));
                floor.setName(rs.getString("name"));
                floor.setDeleted(rs.getBoolean("is_deleted"));
                return floor;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insert new floor
    public boolean insertFloor(Floor floor) {
        String sql = "INSERT INTO floors (building_id, floor_number, name, is_deleted) VALUES (?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, floor.getBuildingId());
            pstmt.setInt(2, floor.getFloorNumber());
            pstmt.setString(3, floor.getName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update floor
    public boolean updateFloor(Floor floor) {
        String sql = "UPDATE floors SET building_id = ?, floor_number = ?, name = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, floor.getBuildingId());
            pstmt.setInt(2, floor.getFloorNumber());
            pstmt.setString(3, floor.getName());
            pstmt.setLong(4, floor.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete floor
    public boolean deleteFloor(Long id) {
        String sql = "UPDATE floors SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static class FloorStats {
        public int totalApartments = 0;
        public int rentedApartments = 0;
        
        public int getOccupancyRate() {
            if (totalApartments == 0) return 0;
            return (rentedApartments * 100) / totalApartments;
        }
    }

    public FloorStats getFloorStatistics(Long floorId) {
        FloorStats stats = new FloorStats();
        
        // 1. Đếm tổng số căn hộ trong tầng
        String sqlTotal = "SELECT COUNT(*) FROM apartments WHERE floor_id = ? AND is_deleted = 0";
        
        // 2. Đếm số căn hộ ĐANG THUÊ (Giả sử dựa vào bảng contracts hoặc trạng thái apartment)
        // Cách 1: Nếu bảng apartments có cột status ('RENTED', 'AVAILABLE')
        // String sqlRented = "SELECT COUNT(*) FROM apartments WHERE floor_id = ? AND status = 'RENTED' AND is_deleted = 0";
        
        // Cách 2: Nếu phải join bảng contracts (Chính xác hơn nếu quản lý theo hợp đồng)
        String sqlRented = "SELECT COUNT(DISTINCT a.id) FROM apartments a " +
                           "JOIN contracts c ON a.id = c.apartment_id " +
                           "WHERE a.floor_id = ? AND c.status = 'ACTIVE' AND c.is_deleted = 0";

        try (Connection conn = Db_connection.getConnection()) {
            // Query Tổng
            try (PreparedStatement pst1 = conn.prepareStatement(sqlTotal)) {
                pst1.setLong(1, floorId);
                try (ResultSet rs = pst1.executeQuery()) {
                    if (rs.next()) stats.totalApartments = rs.getInt(1);
                }
            }
            
            // Query Đang thuê
            try (PreparedStatement pst2 = conn.prepareStatement(sqlRented)) {
                pst2.setLong(1, floorId);
                try (ResultSet rs = pst2.executeQuery()) {
                    if (rs.next()) stats.rentedApartments = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
}