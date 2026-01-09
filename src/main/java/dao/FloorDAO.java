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
}