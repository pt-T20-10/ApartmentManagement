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
        building.setDescription(rs.getString("description"));
        building.setStatus(rs.getString("status"));
        building.setDeleted(rs.getBoolean("is_deleted"));
        
        // [MỚI] Map thông tin quản lý từ kết quả JOIN
        building.setManagerUserId(rs.getLong("manager_user_id"));
        // Lấy tên từ bảng users (đã được join)
        try {
            building.setManagerName(rs.getString("manager_full_name"));
        } catch (SQLException e) {
            // Trường hợp query không join, để trống tên hoặc set mặc định
            building.setManagerName("N/A");
        }
        
        return building;
    }

    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        // [MỚI] JOIN với bảng users để lấy tên người quản lý
        String sql = "SELECT b.*, u.full_name as manager_full_name " +
                     "FROM buildings b " +
                     "LEFT JOIN users u ON b.manager_user_id = u.id " +
                     "WHERE b.is_deleted = 0 ORDER BY b.id DESC";
                     
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
        // [MỚI] Insert vào cột manager_user_id thay vì manager_name
        String sql = "INSERT INTO buildings (name, address, manager_user_id, description, status, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            
            // Set ID người quản lý (nếu null thì set NULL hoặc mặc định)
            if (building.getManagerUserId() != null) {
                pstmt.setLong(3, building.getManagerUserId());
            } else {
                pstmt.setNull(3, Types.BIGINT);
            }
            
            pstmt.setString(4, building.getDescription());
            pstmt.setString(5, building.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean updateBuilding(Building building) {
        // [MỚI] Update cột manager_user_id
        String sql = "UPDATE buildings SET name=?, address=?, manager_user_id=?, description=?, status=? WHERE id=?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, building.getName());
            pstmt.setString(2, building.getAddress());
            
            if (building.getManagerUserId() != null) {
                pstmt.setLong(3, building.getManagerUserId());
            } else {
                pstmt.setNull(3, Types.BIGINT);
            }
            
            pstmt.setString(4, building.getDescription());
            pstmt.setString(5, building.getStatus());
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
    public boolean hasActiveContracts(Long buildingId) {
        // JOIN 3 bảng: Contracts -> Apartments -> Floors -> Buildings
        String sql = "SELECT COUNT(*) FROM contracts c " +
                     "JOIN apartments a ON c.apartment_id = a.id " +
                     "JOIN floors f ON a.floor_id = f.id " +
                     "WHERE f.building_id = ? " +
                     "AND c.status = 'ACTIVE' " +  // Chỉ tính hợp đồng đang hiệu lực
                     "AND c.is_deleted = 0";       // Và hợp đồng chưa bị xóa

       try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            pstmt.setLong(1, buildingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Trả về true nếu có > 0 hợp đồng
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateStatusCascade(Long buildingId, String newStatus) {
        Connection conn = null;
        PreparedStatement pstBuilding = null;
        PreparedStatement pstFloors = null;
        PreparedStatement pstApartments = null;
        
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction (để đảm bảo an toàn dữ liệu)

            // 1. Cập nhật trạng thái Tòa nhà
            String sqlBuilding = "UPDATE buildings SET status = ? WHERE id = ?";
            pstBuilding = conn.prepareStatement(sqlBuilding);
            pstBuilding.setString(1, newStatus);
            pstBuilding.setLong(2, buildingId);
            pstBuilding.executeUpdate();

            // 2. Xử lý Logic Lan truyền
            if ("MAINTENANCE".equalsIgnoreCase(newStatus)) {
                // === CHUYỂN SANG BẢO TRÌ ===
                
                // Tầng -> MAINTENANCE
                String sqlFloor = "UPDATE floors SET status = 'MAINTENANCE' WHERE building_id = ? AND is_deleted = 0";
                pstFloors = conn.prepareStatement(sqlFloor);
                pstFloors.setLong(1, buildingId);
                pstFloors.executeUpdate();

                // Căn hộ -> MAINTENANCE (Chỉ cập nhật những căn đang Trống hoặc Bảo trì, KHÔNG đụng vào căn đã xóa)
                // Lưu ý: Logic check hợp đồng active đã được làm ở Dialog, nên ở đây cứ update thoải mái
                String sqlApt = "UPDATE apartments SET status = 'MAINTENANCE' " +
                                "WHERE floor_id IN (SELECT id FROM floors WHERE building_id = ?) " +
                                "AND is_deleted = 0";
                pstApartments = conn.prepareStatement(sqlApt);
                pstApartments.setLong(1, buildingId);
                pstApartments.executeUpdate();

            } else if ("ACTIVE".equalsIgnoreCase(newStatus)) {
                // === CHUYỂN SANG HOẠT ĐỘNG ===
                
                // Tầng -> ACTIVE (Hoạt động)
                String sqlFloor = "UPDATE floors SET status = 'ACTIVE' WHERE building_id = ? AND is_deleted = 0";
                pstFloors = conn.prepareStatement(sqlFloor);
                pstFloors.setLong(1, buildingId);
                pstFloors.executeUpdate();

                // Căn hộ -> AVAILABLE (Trống)
                // CHỈ mở khóa những căn đang ở trạng thái MAINTENANCE. 
                // Nếu căn đó đang là RENTED (logic lỗi) hoặc trạng thái khác thì giữ nguyên cho an toàn.
                String sqlApt = "UPDATE apartments SET status = 'AVAILABLE' " +
                                "WHERE floor_id IN (SELECT id FROM floors WHERE building_id = ?) " +
                                "AND status = 'MAINTENANCE' " + // Chỉ mở những căn đang bị khóa bảo trì
                                "AND is_deleted = 0";
                pstApartments = conn.prepareStatement(sqlApt);
                pstApartments.setLong(1, buildingId);
                pstApartments.executeUpdate();
            }

            conn.commit(); // Xác nhận lưu tất cả thay đổi
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Hoàn tác nếu lỗi
            return false;
        } finally {
            try {
                if (pstBuilding != null) pstBuilding.close();
                if (pstFloors != null) pstFloors.close();
                if (pstApartments != null) pstApartments.close();
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}