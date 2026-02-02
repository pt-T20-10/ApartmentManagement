package dao;

import model.Contract;
import model.ContractHistory;
import model.User;
import connection.Db_connection;
import util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ContractDAO - FINAL FULL VERSION
 * Fix: insertContract returns generated ID.
 * All other methods kept intact.
 */
public class ContractDAO {

    private ContractHistoryDAO contractHistoryDAO = new ContractHistoryDAO();

    // --- HELPER: Get current user ID safely ---
    private Long getCurrentUserId() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                return currentUser.getId();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Cannot get current user ID: " + e.getMessage());
        }
        return null;
    }

    // --- HELPER: Mapping ResultSet to Contract (UPDATED) ---
    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getLong("id"));
        contract.setContractNumber(rs.getString("contract_number"));
        contract.setApartmentId(rs.getLong("apartment_id"));
        contract.setResidentId(rs.getLong("resident_id"));
        contract.setContractType(rs.getString("contract_type"));

        // Signed date
        java.sql.Date signedDate = rs.getDate("signed_date");
        if (signedDate != null) {
            contract.setSignedDate(new java.util.Date(signedDate.getTime()));
        }

        // Start date (nullable for OWNERSHIP)
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            contract.setStartDate(new java.util.Date(startDate.getTime()));
        }

        // End date (nullable for OWNERSHIP)
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            contract.setEndDate(new java.util.Date(endDate.getTime()));
        }

        // Terminated date
        java.sql.Date terminatedDate = rs.getDate("terminated_date");
        if (terminatedDate != null) {
            contract.setTerminatedDate(new java.util.Date(terminatedDate.getTime()));
        }

        // Financial
        contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        contract.setMonthlyRent(rs.getBigDecimal("monthly_rent")); // ✅ Dual purpose field

        // Status and notes
        contract.setStatus(rs.getString("status"));
        contract.setNotes(rs.getString("notes"));
        contract.setDeleted(rs.getBoolean("is_deleted"));

        // Timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            contract.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            contract.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }

        return contract;
    }

    /**
     * Get all contracts with building filter
     */
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts WHERE is_deleted = 0 ORDER BY created_at DESC";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contracts.add(mapResultSetToContract(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    // --- GET CONTRACT BY ID ---
    public Contract getContractById(Long id) {
        String sql = "SELECT * FROM contracts WHERE id = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToContract(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- GET CONTRACT BY CONTRACT NUMBER ---
    public Contract getContractByNumber(String contractNumber) {
        String sql = "SELECT * FROM contracts WHERE contract_number = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, contractNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToContract(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- GET CONTRACTS BY BUILDING ---
    public List<Contract> getContractsByBuilding(Long buildingId) {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT c.* FROM contracts c "
                + "INNER JOIN apartments a ON c.apartment_id = a.id "
                + "INNER JOIN floors f ON a.floor_id = f.id "
                + "WHERE f.building_id = ? AND c.is_deleted = 0 "
                + "ORDER BY c.created_at DESC";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, buildingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapResultSetToContract(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    // --- GET ACTIVE CONTRACTS BY APARTMENT ---
    public List<Contract> getActiveContractsByApartment(Long apartmentId) {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts "
                + "WHERE apartment_id = ? "
                + "AND status = 'ACTIVE' "
                + "AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, apartmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapResultSetToContract(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    // --- GET SINGLE ACTIVE CONTRACT BY APARTMENT (WITH TENANT INFO) ---
    public Contract getActiveContractByApartmentId(Long apartmentId) {
        String sql
                = "SELECT c.*, r.full_name AS tenant_name, r.phone AS tenant_phone "
                + "FROM contracts c "
                + "JOIN residents r ON c.resident_id = r.id "
                + "WHERE c.apartment_id = ? "
                + "  AND c.status = 'ACTIVE' "
                + "  AND c.is_deleted = 0 "
                + "ORDER BY c.created_at DESC LIMIT 1";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, apartmentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Contract c = mapResultSetToContract(rs);
                    try {
                        c.setTenantName(rs.getString("tenant_name"));
                        c.setTenantPhone(rs.getString("tenant_phone"));
                    } catch (SQLException e) {
                        System.out.println("Tenant info not available: " + e.getMessage());
                    }
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- CHECK IF APARTMENT HAS ACTIVE CONTRACT ---
    public boolean hasActiveContract(Long apartmentId) {
        List<Contract> activeContracts = getActiveContractsByApartment(apartmentId);
        return !activeContracts.isEmpty();
    }

    // --- ✅ FIX: INSERT CONTRACT (RETURN GENERATED KEY) ---
    public boolean insertContract(Contract contract) {
        String sql = "INSERT INTO contracts (apartment_id, resident_id, contract_number, "
                + "contract_type, start_date, end_date, signed_date, monthly_rent, "
                + "deposit_amount, status, created_at, is_deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', NOW(), 0)";

        try (Connection conn = Db_connection.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                // Thêm RETURN_GENERATED_KEYS để lấy ID vừa tạo
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setLong(1, contract.getApartmentId());
                    pstmt.setLong(2, contract.getResidentId());
                    pstmt.setString(3, contract.getContractNumber());
                    pstmt.setString(4, contract.getContractType());

                    if (contract.getStartDate() != null) {
                        pstmt.setDate(5, new java.sql.Date(contract.getStartDate().getTime()));
                    } else {
                        pstmt.setNull(5, java.sql.Types.DATE);
                    }

                    if (contract.getEndDate() != null) {
                        pstmt.setDate(6, new java.sql.Date(contract.getEndDate().getTime()));
                    } else {
                        pstmt.setNull(6, java.sql.Types.DATE);
                    }

                    if (contract.getSignedDate() != null) {
                        pstmt.setDate(7, new java.sql.Date(contract.getSignedDate().getTime()));
                    } else {
                        pstmt.setNull(7, java.sql.Types.DATE);
                    }

                    pstmt.setBigDecimal(8, contract.getMonthlyRent());
                    pstmt.setBigDecimal(9, contract.getDepositAmount());

                    int result = pstmt.executeUpdate();

                    if (result == 0) {
                        conn.rollback();
                        return false;
                    }

                    // ✅ QUAN TRỌNG: Lấy ID và set lại vào object contract
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            contract.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating contract failed, no ID obtained.");
                        }
                    }
                }

                // Cập nhật trạng thái Căn hộ (Đã thuê/Đã bán)
                boolean statusUpdated = updateApartmentStatusOnContractCreate(
                        conn,
                        contract.getApartmentId(),
                        contract.getContractType()
                );

                if (!statusUpdated) {
                    conn.rollback();
                    return false;
                }

                conn.commit(); // Commit Transaction
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ UPDATE CONTRACT - UPDATED WITH NULL CHECKS FOR DATES
    public boolean updateContract(Contract contract) {
        String sql = "UPDATE contracts SET "
                + "contract_number = ?, apartment_id = ?, resident_id = ?, contract_type = ?, "
                + "signed_date = ?, start_date = ?, end_date = ?, terminated_date = ?, "
                + "deposit_amount = ?, monthly_rent = ?, status = ?, notes = ? "
                + "WHERE id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            pstmt.setString(idx++, contract.getContractNumber());
            pstmt.setLong(idx++, contract.getApartmentId());
            pstmt.setLong(idx++, contract.getResidentId());
            pstmt.setString(idx++, contract.getContractType());

            // ✅ signed_date (nullable)
            if (contract.getSignedDate() != null) {
                pstmt.setDate(idx++, new java.sql.Date(contract.getSignedDate().getTime()));
            } else {
                pstmt.setNull(idx++, Types.DATE);
            }

            // ✅ start_date (NULL for OWNERSHIP)
            if (contract.getStartDate() != null) {
                pstmt.setDate(idx++, new java.sql.Date(contract.getStartDate().getTime()));
            } else {
                pstmt.setNull(idx++, Types.DATE);
            }

            // ✅ end_date (NULL for OWNERSHIP)
            if (contract.getEndDate() != null) {
                pstmt.setDate(idx++, new java.sql.Date(contract.getEndDate().getTime()));
            } else {
                pstmt.setNull(idx++, Types.DATE);
            }

            // ✅ terminated_date (nullable)
            if (contract.getTerminatedDate() != null) {
                pstmt.setDate(idx++, new java.sql.Date(contract.getTerminatedDate().getTime()));
            } else {
                pstmt.setNull(idx++, Types.DATE);
            }

            pstmt.setBigDecimal(idx++, contract.getDepositAmount());
            pstmt.setBigDecimal(idx++, contract.getMonthlyRent()); // ✅ Dual purpose
            pstmt.setString(idx++, contract.getStatus());
            pstmt.setString(idx++, contract.getNotes());
            pstmt.setLong(idx++, contract.getId());

            boolean success = pstmt.executeUpdate() > 0;

            // LOG TO HISTORY - UPDATED
            if (success) {
                try {
                    ContractHistory history = new ContractHistory();
                    history.setContractId(contract.getId());
                    history.setAction("UPDATED");
                    history.setReason("Cập nhật thông tin hợp đồng");
                    history.setCreatedBy(getCurrentUserId());
                    contractHistoryDAO.insert(history);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to log history: " + e.getMessage());
                }
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating contract: " + e.getMessage());
        }
        return false;
    }

    private boolean updateApartmentStatusOnContractCreate(Connection conn, Long apartmentId, String contractType)
            throws SQLException {

        String newStatus = "RENTAL".equals(contractType) ? "RENTED" : "OWNED";

        String sql = "UPDATE apartments SET status = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setLong(2, apartmentId);

            int result = pstmt.executeUpdate();
            return result > 0;
        }
    }

    // ✅ RENEW CONTRACT - ONLY FOR RENTAL CONTRACTS
    public boolean renewContract(Long contractId, java.util.Date newEndDate) {
        // Get contract and validate it's RENTAL
        Contract contract = getContractById(contractId);
        if (contract == null) {
            System.err.println("Contract not found: " + contractId);
            return false;
        }

        if (!contract.isRental()) {
            System.err.println("Cannot renew OWNERSHIP contract: " + contractId);
            return false;
        }

        java.util.Date oldEndDate = contract.getEndDate();

        String sql = "UPDATE contracts SET end_date = ? WHERE id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(newEndDate.getTime()));
            pstmt.setLong(2, contractId);

            boolean success = pstmt.executeUpdate() > 0;

            // LOG TO HISTORY - RENEWED
            if (success) {
                try {
                    ContractHistory history = new ContractHistory();
                    history.setContractId(contractId);
                    history.setAction("RENEWED");
                    history.setOldEndDate(oldEndDate);
                    history.setNewEndDate(newEndDate);
                    history.setReason("Gia hạn hợp đồng");
                    history.setCreatedBy(getCurrentUserId());
                    contractHistoryDAO.insert(history);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to log history: " + e.getMessage());
                }
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- TERMINATE CONTRACT (FOR BOTH RENTAL AND OWNERSHIP) ---
    public boolean terminateContract(Long contractId) {
        String sqlGetApartment = "SELECT apartment_id FROM contracts WHERE id = ?";
        String sqlUpdateContract = "UPDATE contracts SET status = 'TERMINATED' WHERE id = ?";
        String sqlUpdateApartment = "UPDATE apartments SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = Db_connection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Long apartmentId = null;

                // 1. Get apartment ID
                try (PreparedStatement pstmt = conn.prepareStatement(sqlGetApartment)) {
                    pstmt.setLong(1, contractId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        apartmentId = rs.getLong("apartment_id");
                    }
                }

                if (apartmentId == null) {
                    conn.rollback();
                    return false;
                }

                // 2. Update contract status
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateContract)) {
                    pstmt.setLong(1, contractId);
                    pstmt.executeUpdate();
                }

                // 3. Update apartment status to AVAILABLE
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateApartment)) {
                    pstmt.setLong(1, apartmentId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE CONTRACT ---
    public boolean deleteContract(Long contractId) {
        String sqlGetAptId = "SELECT apartment_id FROM contracts WHERE id = ?";
        String sqlDeleteContract = "UPDATE contracts SET is_deleted = 1 WHERE id = ?";
        String sqlResetApartment = "UPDATE apartments SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = Db_connection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Get apartment ID
                Long apartmentId = null;
                try (PreparedStatement pstG = conn.prepareStatement(sqlGetAptId)) {
                    pstG.setLong(1, contractId);
                    try (ResultSet rs = pstG.executeQuery()) {
                        if (rs.next()) {
                            apartmentId = rs.getLong("apartment_id");
                        }
                    }
                }

                // 2. Soft delete contract
                try (PreparedStatement pstD = conn.prepareStatement(sqlDeleteContract)) {
                    pstD.setLong(1, contractId);
                    pstD.executeUpdate();
                }

                // 3. Reset apartment status
                if (apartmentId != null) {
                    try (PreparedStatement pstR = conn.prepareStatement(sqlResetApartment)) {
                        pstR.setLong(1, apartmentId);
                        pstR.executeUpdate();
                    }
                }

                // 4. LOG TO HISTORY
                ContractHistory history = new ContractHistory();
                history.setContractId(contractId);
                history.setAction("DELETED");
                history.setReason("Xóa hợp đồng");
                history.setCreatedBy(getCurrentUserId());
                // Chú ý: Cần đảm bảo contractHistoryDAO.insert hỗ trợ connection nếu muốn cùng transaction
                // Nếu không, có thể gọi phiên bản thường, rủi ro nhỏ nếu lỗi ở history nhưng contract đã xóa.
                contractHistoryDAO.insert(history); 

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Count contracts by status with building filter
     */
    public int countContractsByStatus(String status) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        String sql = "SELECT COUNT(*) FROM contracts c " +
                     "JOIN apartments a ON c.apartment_id = a.id " +
                     "JOIN floors f ON a.floor_id = f.id " +
                     "WHERE c.status = ? AND c.is_deleted = 0 ";
        
        if (currentUser != null && !currentUser.isAdmin() && currentUser.getBuildingId() != null) {
            sql += "AND f.building_id = ?";
        }
        
        try (Connection conn = Db_connection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            
            if (currentUser != null && !currentUser.isAdmin() && currentUser.getBuildingId() != null) {
                ps.setLong(2, currentUser.getBuildingId());
            }
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countActiveContracts() {
        return countContractsByStatus("ACTIVE");
    }

    /**
     * Get expiring contracts with building filter
     */
    public List<Contract> getExpiringContracts(int daysThreshold) {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts "
                + "WHERE contract_type = 'RENTAL' "
                + // ✅ Only RENTAL contracts expire
                "AND end_date IS NOT NULL "
                + "AND DATEDIFF(end_date, CURDATE()) BETWEEN 0 AND ? "
                + "AND status = 'ACTIVE' "
                + "AND is_deleted = 0 "
                + "ORDER BY end_date ASC";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, daysThreshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapResultSetToContract(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    // --- COUNT INVOICES BY CONTRACT ---
    public int countInvoicesByContract(Long contractId) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE contract_id = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, contractId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- GENERATE CONTRACT NUMBER ---
    public String generateContractNumber() {
        String sql = "SELECT IFNULL(MAX(CAST(SUBSTRING(contract_number, 12, 3) AS UNSIGNED)), 0) + 1 AS next_seq "
                + "FROM contracts "
                + "WHERE contract_number LIKE CONCAT('HD', DATE_FORMAT(CURDATE(), '%Y%m%d'), '%')";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int nextSeq = rs.getInt("next_seq");
                String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
                return String.format("HD%s%03d", dateStr, nextSeq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fallback
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        return String.format("HD%s001", dateStr);
    }
} //hehe
