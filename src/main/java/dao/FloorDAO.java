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
    
    private Floor mapResultSetToFloor(ResultSet rs) throws SQLException {
        Floor floor = new Floor();
        floor.setId(rs.getLong("id"));
        floor.setBuildingId(rs.getLong("building_id"));
        floor.setFloorNumber(rs.getInt("floor_number"));
        floor.setName(rs.getString("name"));
        floor.setStatus(rs.getString("status"));
        floor.setDeleted(rs.getBoolean("is_deleted")); // ⚠️ CỘT NÀY PHẢI CÓ TRONG SELECT
        return floor;
    }

    // --- HÀM TỐI ƯU HÓA TỐC ĐỘ - DỰA VÀO apartments.status ---
    public List<FloorWithStats> getFloorsWithStatsByBuildingId(Long buildingId) {
        List<FloorWithStats> results = new ArrayList<>();

        // ✅ FIX DUY NHẤT: BỔ SUNG f.is_deleted AS is_deleted
        String sql =
            "SELECT f.id, f.building_id, f.floor_number, f.name, f.status, f.is_deleted AS is_deleted, " +
            "       COALESCE(apt_stats.total_apts, 0)  AS total_apts, " +
            "       COALESCE(apt_stats.rented_apts, 0) AS rented_apts " +
            "FROM floors f " +
            "LEFT JOIN ( " +
            "    SELECT a.floor_id, " +
            "           COUNT(a.id) AS total_apts, " +
            "           SUM(CASE WHEN a.status = 'RENTED' THEN 1 ELSE 0 END) AS rented_apts " +
            "    FROM apartments a " +
            "    WHERE a.is_deleted = 0 " +
            "    GROUP BY a.floor_id " +
            ") apt_stats ON f.id = apt_stats.floor_id " +
            "WHERE f.building_id = ? AND f.is_deleted = 0 " +
            "ORDER BY f.floor_number ASC";

        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Floor floor = mapResultSetToFloor(rs);

                    FloorStats stats = new FloorStats();
                    stats.totalApartments  = rs.getInt("total_apts");
                    stats.rentedApartments = rs.getInt("rented_apts");

                    results.add(new FloorWithStats(floor, stats));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    // --- CÁC HÀM CẦN THIẾT ---

    public Floor getFloorById(Long id) {
        String sql = "SELECT * FROM floors WHERE id = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToFloor(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFloorNameExists(Long buildingId, String name) {
        String sql = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND name = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            pstmt.setString(2, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFloorNameExists(Long buildingId, String name, Long excludeFloorId) {
        String sql = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND name = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            pstmt.setString(2, name);
            pstmt.setLong(3, excludeFloorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFloorNumberExists(Long buildingId, int floorNumber) {
        String sql = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND floor_number = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            pstmt.setInt(2, floorNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFloorNumberExists(Long buildingId, int floorNumber, Long excludeFloorId) {
        String sql = "SELECT COUNT(*) FROM floors WHERE building_id = ? AND floor_number = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            pstmt.setInt(2, floorNumber);
            pstmt.setLong(3, excludeFloorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- CRUD KHÁC ---

    public boolean insertFloor(Floor floor) {
        String sql = "INSERT INTO floors (building_id, floor_number, name, status, is_deleted) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, floor.getBuildingId());
            pstmt.setInt(2, floor.getFloorNumber());
            pstmt.setString(3, floor.getName());
            pstmt.setString(4, floor.getStatus());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFloor(Floor floor) {
        String sql = "UPDATE floors SET floor_number = ?, name = ?, status = ? WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, floor.getFloorNumber());
            pstmt.setString(2, floor.getName());
            pstmt.setString(3, floor.getStatus());
            pstmt.setLong(4, floor.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    // --- CLASS PHỤ ---

    public static class FloorStats {
        public int totalApartments = 0;
        public int rentedApartments = 0;

        public int getAvailableApartments() {
            return totalApartments - rentedApartments;
        }

        public int getOccupancyRate() {
            return (totalApartments == 0) ? 0 : (rentedApartments * 100) / totalApartments;
        }
    }

    public static class FloorWithStats {
        public Floor floor;
        public FloorStats stats;

        public FloorWithStats(Floor f, FloorStats s) {
            this.floor = f;
            this.stats = s;
        }
    }
    public List<Floor> getFloorsByBuildingId(Long buildingId) {
    List<Floor> floors = new ArrayList<>();
    String sql = "SELECT * FROM floors WHERE building_id = ? AND is_deleted = 0 ORDER BY floor_number";

    try (Connection conn = Db_connection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setLong(1, buildingId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                floors.add(mapResultSetToFloor(rs));
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return floors;
}

}
