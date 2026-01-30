package dao;

import model.Apartment;
import connection.Db_connection;
import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Apartment operations Note: base_price removed - pricing handled
 * in contracts
 */
public class ApartmentDAO {

    // --- HELPER: Map ResultSet to Object (Tránh lặp code) ---
    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        Apartment apartment = new Apartment();
        apartment.setId(rs.getLong("id"));
        apartment.setFloorId(rs.getLong("floor_id"));
        apartment.setRoomNumber(rs.getString("room_number"));
        apartment.setArea(rs.getDouble("area"));
        apartment.setStatus(rs.getString("status"));
        apartment.setDescription(rs.getString("description"));
        apartment.setDeleted(rs.getBoolean("is_deleted"));

        // --- 3 THUỘC TÍNH MỚI ---
        apartment.setApartmentType(rs.getString("apartment_type"));
        apartment.setBedroomCount(rs.getInt("bedroom_count"));
        apartment.setBathroomCount(rs.getInt("bathroom_count"));

        return apartment;
    }

    // Get all apartments
    public List<Apartment> getAllApartments() {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE is_deleted = 0 ORDER BY room_number";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                apartments.add(mapResultSetToApartment(rs));
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

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, floorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apartments.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }

    public int countOwnedApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = 'OWNED' AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Apartment> getOwnedApartments() {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE status = 'OWNED' AND is_deleted = 0 ORDER BY room_number";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                apartments.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }

    // Get apartments by building ID (through floor relationship)
    public List<Apartment> getApartmentsByBuildingId(Long buildingId) {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT a.* FROM apartments a "
                + "JOIN floors f ON a.floor_id = f.id "
                + "WHERE f.building_id = ? AND a.is_deleted = 0 "
                + "ORDER BY f.floor_number, a.room_number";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apartments.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }

    // Get apartment by ID
    public Apartment getApartmentById(Long id) {
        String sql = "SELECT * FROM apartments WHERE id = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToApartment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Insert new apartment (ĐÃ BỎ base_price)
    public boolean insertApartment(Apartment apartment) {
        String sql = "INSERT INTO apartments (floor_id, room_number, area, status, description, "
                + "apartment_type, bedroom_count, bathroom_count, is_deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, apartment.getFloorId());
            pstmt.setString(2, apartment.getRoomNumber());
            pstmt.setDouble(3, apartment.getArea());
            pstmt.setString(4, apartment.getStatus());
            pstmt.setString(5, apartment.getDescription());

            // --- Set 3 tham số mới ---
            pstmt.setString(6, apartment.getApartmentType());
            pstmt.setInt(7, apartment.getBedroomCount());
            pstmt.setInt(8, apartment.getBathroomCount());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update apartment (ĐÃ BỎ base_price)
    public boolean updateApartment(Apartment apartment) {
        String sql = "UPDATE apartments SET floor_id = ?, room_number = ?, area = ?, status = ?, "
                + "description = ?, apartment_type = ?, bedroom_count = ?, bathroom_count = ? "
                + "WHERE id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, apartment.getFloorId());
            pstmt.setString(2, apartment.getRoomNumber());
            pstmt.setDouble(3, apartment.getArea());
            pstmt.setString(4, apartment.getStatus());
            pstmt.setString(5, apartment.getDescription());

            // --- Set 3 tham số mới ---
            pstmt.setString(6, apartment.getApartmentType());
            pstmt.setInt(7, apartment.getBedroomCount());
            pstmt.setInt(8, apartment.getBathroomCount());

            pstmt.setLong(9, apartment.getId()); // ID là tham số cuối cùng

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Soft delete apartment
    public boolean deleteApartment(Long id) {
        String sql = "UPDATE apartments SET is_deleted = 1 WHERE id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Count functions...
    public int countApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE is_deleted = 0";
        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countApartmentsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    public int countAvailableApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = 'AVAILABLE' AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countRentedApartments() {
        String sql = "SELECT COUNT(*) FROM apartments WHERE status = 'RENTED' AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getStatusDisplay(String status) {
        if (status == null) {
            return "N/A";
        }

        switch (status.toUpperCase()) {
            case "AVAILABLE":
                return "TRỐNG";
            case "RENTED":
                return "ĐÃ THUÊ";
            case "OWNED":
                return "ĐÃ BÁN";
            default:
                return status;
        }
    }

    public static Color getStatusColor(String status) {
        if (status == null) {
            return Color.GRAY;
        }

        switch (status.toUpperCase()) {
            case "AVAILABLE":
                return new Color(76, 175, 80);  // Green
            case "RENTED":
                return new Color(255, 152, 0);  // Orange
            case "OWNED":
                return new Color(156, 39, 176); // Purple
            default:
                return Color.GRAY;
        }
    }

    public boolean hasHistory(Long apartmentId) {
        // Kiểm tra xem căn hộ này đã từng có hợp đồng nào chưa (kể cả đã kết thúc)
        String sql = "SELECT COUNT(*) FROM contracts WHERE apartment_id = ?";
        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, apartmentId);
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
}
