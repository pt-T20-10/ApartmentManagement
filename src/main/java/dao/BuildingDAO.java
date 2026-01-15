package dao;

import model.Building;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDAO {
    
    private Building mapResultSetToBuilding(ResultSet rs) throws SQLException {
        Building building = new Building();
        building.setId(rs.getLong("id"));
        building.setName(rs.getString("name"));
        building.setAddress(rs.getString("address"));
        building.setManagerName(rs.getString("manager_name"));
        building.setDescription(rs.getString("description"));
        building.setStatus(rs.getString("status"));
        // Đã XÓA hoàn toàn operation_date
        building.setDeleted(rs.getBoolean("is_deleted"));
        return building;
    }

    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE is_deleted = 0 ORDER BY id DESC";
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) buildings.add(mapResultSetToBuilding(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return buildings;
    }
    
    public Building getBuildingById(Long id) {
        String sql = "SELECT * FROM buildings WHERE id = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToBuilding(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insertBuilding(Building building) {
        // XÓA operation_date khỏi câu lệnh SQL
        String sql = "INSERT INTO buildings (name, address, manager_name, description, status, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            pstmt.setString(3, building.getManagerName());
            pstmt.setString(4, building.getDescription());
            pstmt.setString(5, building.getStatus());
            // Bỏ dòng setDate
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean updateBuilding(Building building) {
        // XÓA operation_date khỏi câu lệnh SQL
        String sql = "UPDATE buildings SET name=?, address=?, manager_name=?, description=?, status=? WHERE id=?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            pstmt.setString(3, building.getManagerName());
            pstmt.setString(4, building.getDescription());
            pstmt.setString(5, building.getStatus());
            // Bỏ dòng setDate, setLong id giờ là tham số thứ 6
            pstmt.setLong(6, building.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean deleteBuilding(Long id) {
        String sql = "UPDATE buildings SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean addBuilding(Building building) { return insertBuilding(building); }
    
    public List<Building> searchBuildingsByName(String keyword) {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE (name LIKE ? OR address LIKE ?) AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            pstmt.setString(1, query); pstmt.setString(2, query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) buildings.add(mapResultSetToBuilding(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return buildings;
    }

    // --- THỐNG KÊ ---
    public static class BuildingStats {
        public int totalFloors = 0;
        public int totalApartments = 0;
        public int rentedApartments = 0;
        public int getOccupancyRate() {
            if (totalApartments == 0) return 0;
            return (rentedApartments * 100) / totalApartments;
        }
    }
    
    public BuildingStats getBuildingStatistics(Long buildingId) {
        BuildingStats stats = new BuildingStats();
        String sqlFloors = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND is_deleted = 0";
        String sqlApts = "SELECT COUNT(*) FROM apartments a JOIN floors f ON a.floor_id = f.id WHERE f.building_id = ? AND a.is_deleted = 0";
        String sqlRented = "SELECT COUNT(DISTINCT c.apartment_id) FROM contracts c JOIN apartments a ON c.apartment_id = a.id JOIN floors f ON a.floor_id = f.id WHERE f.building_id = ? AND c.status = 'ACTIVE' AND c.is_deleted = 0";

        try (Connection conn = Db_connection.getConnection()) {
            try (PreparedStatement pst1 = conn.prepareStatement(sqlFloors)) {
                pst1.setLong(1, buildingId);
                try (ResultSet rs = pst1.executeQuery()) { if (rs.next()) stats.totalFloors = rs.getInt(1); }
            }
            try (PreparedStatement pst2 = conn.prepareStatement(sqlApts)) {
                pst2.setLong(1, buildingId);
                try (ResultSet rs = pst2.executeQuery()) { if (rs.next()) stats.totalApartments = rs.getInt(1); }
            }
            try (PreparedStatement pst3 = conn.prepareStatement(sqlRented)) {
                pst3.setLong(1, buildingId);
                try (ResultSet rs = pst3.executeQuery()) { if (rs.next()) stats.rentedApartments = rs.getInt(1); }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
    
    public int countBuildings() {
        String sql = "SELECT COUNT(*) FROM buildings WHERE is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}