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
    
    // --- HELPER: Mapping dữ liệu từ ResultSet sang Model (Cho gọn code) ---
    private Floor mapResultSetToFloor(ResultSet rs) throws SQLException {
        Floor floor = new Floor();
        floor.setId(rs.getLong("id"));
        floor.setBuildingId(rs.getLong("building_id"));
        floor.setFloorNumber(rs.getInt("floor_number"));
        floor.setName(rs.getString("name"));
        // --- LẤY STATUS (MỚI) ---
        floor.setStatus(rs.getString("status"));
        // ------------------------
        floor.setDeleted(rs.getBoolean("is_deleted"));
        return floor;
    }

    // Get all floors
    public List<Floor> getAllFloors() {
        List<Floor> floors = new ArrayList<>();
        String sql = "SELECT * FROM floors WHERE is_deleted = 0 ORDER BY floor_number";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                floors.add(mapResultSetToFloor(rs));
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
                floors.add(mapResultSetToFloor(rs));
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
                return mapResultSetToFloor(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insert new floor (CẬP NHẬT STATUS)
    public boolean insertFloor(Floor floor) {
        // Thêm cột status vào SQL
        String sql = "INSERT INTO floors (building_id, floor_number, name, status, is_deleted) VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, floor.getBuildingId());
            pstmt.setInt(2, floor.getFloorNumber());
            pstmt.setString(3, floor.getName());
            
            // Set Status (Nếu null thì mặc định)
            String status = (floor.getStatus() == null || floor.getStatus().isEmpty()) ? "Đang hoạt động" : floor.getStatus();
            pstmt.setString(4, status);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update floor (CẬP NHẬT STATUS)
    public boolean updateFloor(Floor floor) {
        // Thêm cột status vào SQL
        String sql = "UPDATE floors SET floor_number = ?, name = ?, status = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, floor.getFloorNumber());
            pstmt.setString(2, floor.getName());
            
            // Set Status
            String status = (floor.getStatus() == null || floor.getStatus().isEmpty()) ? "Đang hoạt động" : floor.getStatus();
            pstmt.setString(3, status);
            
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

    // Check duplicate name
    public boolean isFloorNameExists(Long buildingId, String name) {
        String sql = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND name = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, buildingId);
            pstmt.setString(2, name);
            
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
    
    // Statistics Inner Class
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
        
        String sqlTotal = "SELECT COUNT(*) FROM apartments WHERE floor_id = ? AND is_deleted = 0";
        String sqlRented = "SELECT COUNT(DISTINCT a.id) FROM apartments a " +
                           "JOIN contracts c ON a.id = c.apartment_id " +
                           "WHERE a.floor_id = ? AND c.status = 'ACTIVE' AND c.is_deleted = 0";

        try (Connection conn = Db_connection.getConnection()) {
            try (PreparedStatement pst1 = conn.prepareStatement(sqlTotal)) {
                pst1.setLong(1, floorId);
                try (ResultSet rs = pst1.executeQuery()) {
                    if (rs.next()) stats.totalApartments = rs.getInt(1);
                }
            }
            
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