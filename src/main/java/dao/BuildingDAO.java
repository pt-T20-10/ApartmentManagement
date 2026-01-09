package dao;

import model.Building;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Building operations
 */
public class BuildingDAO {
    
    // Get all buildings
    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Building building = new Building();
                building.setId(rs.getLong("id"));
                building.setName(rs.getString("name"));
                building.setAddress(rs.getString("address"));
                building.setManagerName(rs.getString("manager_name"));
                building.setDescription(rs.getString("description"));
                building.setDeleted(rs.getBoolean("is_deleted"));
                buildings.add(building);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buildings;
    }
    
    // Get building by ID
    public Building getBuildingById(Long id) {
        String sql = "SELECT * FROM buildings WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Building building = new Building();
                building.setId(rs.getLong("id"));
                building.setName(rs.getString("name"));
                building.setAddress(rs.getString("address"));
                building.setManagerName(rs.getString("manager_name"));
                building.setDescription(rs.getString("description"));
                building.setDeleted(rs.getBoolean("is_deleted"));
                return building;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insert new building
    public boolean insertBuilding(Building building) {
        String sql = "INSERT INTO buildings (name, address, manager_name, description, is_deleted) VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            pstmt.setString(3, building.getManagerName());
            pstmt.setString(4, building.getDescription());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update building
    public boolean updateBuilding(Building building) {
        String sql = "UPDATE buildings SET name = ?, address = ?, manager_name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            pstmt.setString(3, building.getManagerName());
            pstmt.setString(4, building.getDescription());
            pstmt.setLong(5, building.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete building
    public boolean deleteBuilding(Long id) {
        String sql = "UPDATE buildings SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Count total buildings
    public int countBuildings() {
        String sql = "SELECT COUNT(*) FROM buildings WHERE is_deleted = 0";
        
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
    
    // Add building (alias for insertBuilding)
    public boolean addBuilding(Building building) {
        return insertBuilding(building);
    }
    
    // Search buildings by name
    public List<Building> searchBuildingsByName(String name) {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE name LIKE ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Building building = new Building();
                building.setId(rs.getLong("id"));
                building.setName(rs.getString("name"));
                building.setAddress(rs.getString("address"));
                building.setManagerName(rs.getString("manager_name"));
                building.setDescription(rs.getString("description"));
                building.setDeleted(rs.getBoolean("is_deleted"));
                buildings.add(building);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buildings;
    }
}