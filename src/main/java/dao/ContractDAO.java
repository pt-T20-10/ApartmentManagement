package dao;

import model.Contract;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Contract operations with automatic Apartment status update
 */
public class ContractDAO {

    // --- HELPER: Mapping dữ liệu từ ResultSet sang Model ---
    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getLong("id"));
        contract.setApartmentId(rs.getLong("apartment_id"));
        contract.setResidentId(rs.getLong("resident_id"));
        
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            contract.setStartDate(new java.util.Date(startDate.getTime()));
        }
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            contract.setEndDate(new java.util.Date(endDate.getTime()));
        }
        
        contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        contract.setStatus(rs.getString("status"));
        contract.setDeleted(rs.getBoolean("is_deleted"));
        return contract;
    }

    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts WHERE is_deleted = 0 ORDER BY start_date DESC";
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contracts.add(mapResultSetToContract(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    public Contract getContractById(Long id) {
        String sql = "SELECT * FROM contracts WHERE id = ? AND is_deleted = 0";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToContract(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- INSERT CONTRACT + AUTO UPDATE APARTMENT (TRANSACTION) ---
    public boolean insertContract(Contract contract) {
        String sqlContract = "INSERT INTO contracts (apartment_id, resident_id, start_date, end_date, deposit_amount, status, is_deleted) VALUES (?, ?, ?, ?, ?, ?, 0)";
        String sqlApartment = "UPDATE apartments SET status = 'RENTED' WHERE id = ?";

        Connection conn = null;
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Thêm hợp đồng
            try (PreparedStatement pstC = conn.prepareStatement(sqlContract)) {
                pstC.setLong(1, contract.getApartmentId());
                pstC.setLong(2, contract.getResidentId());
                pstC.setDate(3, new java.sql.Date(contract.getStartDate().getTime()));
                if (contract.getEndDate() != null) {
                    pstC.setDate(4, new java.sql.Date(contract.getEndDate().getTime()));
                } else {
                    pstC.setNull(4, Types.DATE);
                }
                pstC.setBigDecimal(5, contract.getDepositAmount());
                pstC.setString(6, contract.getStatus());
                pstC.executeUpdate();
            }

            // 2. Cập nhật trạng thái căn hộ sang RENTED
            try (PreparedStatement pstA = conn.prepareStatement(sqlApartment)) {
                pstA.setLong(1, contract.getApartmentId());
                pstA.executeUpdate();
            }

            conn.commit(); // Xác nhận lưu mọi thay đổi
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateContract(Contract contract) {
        String sql = "UPDATE contracts SET apartment_id = ?, resident_id = ?, start_date = ?, end_date = ?, deposit_amount = ?, status = ? WHERE id = ?";
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, contract.getApartmentId());
            pstmt.setLong(2, contract.getResidentId());
            pstmt.setDate(3, new java.sql.Date(contract.getStartDate().getTime()));
            if (contract.getEndDate() != null) pstmt.setDate(4, new java.sql.Date(contract.getEndDate().getTime()));
            else pstmt.setNull(4, Types.DATE);
            pstmt.setBigDecimal(5, contract.getDepositAmount());
            pstmt.setString(6, contract.getStatus());
            pstmt.setLong(7, contract.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- DELETE CONTRACT + AUTO RESET APARTMENT (TRANSACTION) ---
    public boolean deleteContract(Long contractId) {
        String sqlGetAptId = "SELECT apartment_id FROM contracts WHERE id = ?";
        String sqlDeleteContract = "UPDATE contracts SET is_deleted = 1 WHERE id = ?";
        String sqlResetApartment = "UPDATE apartments SET status = 'AVAILABLE' WHERE id = ?";

        Connection conn = null;
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false);

            // 1. Lấy ID căn hộ trước khi xóa hợp đồng
            Long apartmentId = null;
            try (PreparedStatement pstG = conn.prepareStatement(sqlGetAptId)) {
                pstG.setLong(1, contractId);
                try (ResultSet rs = pstG.executeQuery()) {
                    if (rs.next()) apartmentId = rs.getLong("apartment_id");
                }
            }

            // 2. Xóa mềm hợp đồng
            try (PreparedStatement pstD = conn.prepareStatement(sqlDeleteContract)) {
                pstD.setLong(1, contractId);
                pstD.executeUpdate();
            }

            // 3. Đưa căn hộ về trạng thái Trống (AVAILABLE)
            if (apartmentId != null) {
                try (PreparedStatement pstR = conn.prepareStatement(sqlResetApartment)) {
                    pstR.setLong(1, apartmentId);
                    pstR.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        }
        return false;
    }

    public int countActiveContracts() {
        String sql = "SELECT COUNT(*) FROM contracts WHERE status = 'ACTIVE' AND is_deleted = 0";
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