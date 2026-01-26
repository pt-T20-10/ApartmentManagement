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
 * DAO class for Contract operations
 * UPDATED: Support for logging to contract_history
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
        return null; // Return null if user is not logged in
    }

    // --- HELPER: Mapping ResultSet to Contract (UPDATED) ---
    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getLong("id"));
        contract.setContractNumber(rs.getString("contract_number"));
        contract.setApartmentId(rs.getLong("apartment_id"));
        contract.setResidentId(rs.getLong("resident_id"));
        contract.setContractType(rs.getString("contract_type"));
        
        java.sql.Date signedDate = rs.getDate("signed_date");
        if (signedDate != null) {
            contract.setSignedDate(new java.util.Date(signedDate.getTime()));
        }
        
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            contract.setStartDate(new java.util.Date(startDate.getTime()));
        }
        
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            contract.setEndDate(new java.util.Date(endDate.getTime()));
        }
        
        java.sql.Date terminatedDate = rs.getDate("terminated_date");
        if (terminatedDate != null) {
            contract.setTerminatedDate(new java.util.Date(terminatedDate.getTime()));
        }
        
        contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        contract.setStatus(rs.getString("status"));
        contract.setNotes(rs.getString("notes"));
        contract.setDeleted(rs.getBoolean("is_deleted"));
        
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

    // --- GET ALL CONTRACTS ---
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts WHERE is_deleted = 0 ORDER BY created_at DESC";
        
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

    // --- GET CONTRACT BY ID ---
    public Contract getContractById(Long id) {
        String sql = "SELECT * FROM contracts WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT c.* FROM contracts c " +
                     "INNER JOIN apartments a ON c.apartment_id = a.id " +
                     "INNER JOIN floors f ON a.floor_id = f.id " +
                     "WHERE f.building_id = ? AND c.is_deleted = 0 " +
                     "ORDER BY c.created_at DESC";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT * FROM contracts " +
                     "WHERE apartment_id = ? " +
                     "AND status IN ('ACTIVE', 'EXPIRING_SOON') " +
                     "AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    
    // --- CHECK IF APARTMENT HAS ACTIVE CONTRACT ---
    public boolean hasActiveContract(Long apartmentId) {
        List<Contract> activeContracts = getActiveContractsByApartment(apartmentId);
        return !activeContracts.isEmpty();
    }

    // --- INSERT CONTRACT (UPDATED WITH LOGGING) ---
public boolean insertContract(Contract contract) {
    String sqlContract = "INSERT INTO contracts " +
                       "(contract_number, apartment_id, resident_id, contract_type, " +
                       "signed_date, start_date, end_date, deposit_amount, status, notes, is_deleted) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
    String sqlApartment = "UPDATE apartments SET status = 'RENTED' WHERE id = ?";

    try (Connection conn = Db_connection.getConnection()) {
        conn.setAutoCommit(false);

        try {
            // 1. Insert contract
            Long generatedId = null;
            try (PreparedStatement pstC = conn.prepareStatement(sqlContract, Statement.RETURN_GENERATED_KEYS)) {
                pstC.setString(1, contract.getContractNumber());
                pstC.setLong(2, contract.getApartmentId());
                pstC.setLong(3, contract.getResidentId());
                pstC.setString(4, contract.getContractType());
                
                if (contract.getSignedDate() != null) {
                    pstC.setDate(5, new java.sql.Date(contract.getSignedDate().getTime()));
                } else {
                    pstC.setNull(5, Types.DATE);
                }
                
                pstC.setDate(6, new java.sql.Date(contract.getStartDate().getTime()));
                
                if (contract.getEndDate() != null) {
                    pstC.setDate(7, new java.sql.Date(contract.getEndDate().getTime()));
                } else {
                    pstC.setNull(7, Types.DATE);
                }
                
                pstC.setBigDecimal(8, contract.getDepositAmount());
                pstC.setString(9, contract.getStatus());
                pstC.setString(10, contract.getNotes());
                
                int affected = pstC.executeUpdate();
                
                if (affected > 0) {
                    try (ResultSet generatedKeys = pstC.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedId = generatedKeys.getLong(1);
                            contract.setId(generatedId);
                        }
                    }
                }
            }

            // 2. Update apartment status to RENTED
            try (PreparedStatement pstA = conn.prepareStatement(sqlApartment)) {
                pstA.setLong(1, contract.getApartmentId());
                pstA.executeUpdate();
            }
            
            // 3. LOG TO HISTORY - CREATED
            if (generatedId != null) {
                ContractHistory history = new ContractHistory();
                history.setContractId(generatedId);
                history.setAction("CREATED");
                history.setReason("Tạo hợp đồng mới");
                history.setCreatedBy(getCurrentUserId());
                contractHistoryDAO.insert(conn, history);
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

   public boolean updateContract(Contract contract) {
    String sql = "UPDATE contracts SET " +
                 "contract_number = ?, apartment_id = ?, resident_id = ?, contract_type = ?, " +
                 "signed_date = ?, start_date = ?, end_date = ?, terminated_date = ?, " +
                 "deposit_amount = ?, status = ?, notes = ? " +
                 "WHERE id = ?";
    
    try (Connection conn = Db_connection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, contract.getContractNumber());
        pstmt.setLong(2, contract.getApartmentId());
        pstmt.setLong(3, contract.getResidentId());
        pstmt.setString(4, contract.getContractType());
        
        if (contract.getSignedDate() != null) {
            pstmt.setDate(5, new java.sql.Date(contract.getSignedDate().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        
        pstmt.setDate(6, new java.sql.Date(contract.getStartDate().getTime()));
        
        if (contract.getEndDate() != null) {
            pstmt.setDate(7, new java.sql.Date(contract.getEndDate().getTime()));
        } else {
            pstmt.setNull(7, Types.DATE);
        }
        
        if (contract.getTerminatedDate() != null) {
            pstmt.setDate(8, new java.sql.Date(contract.getTerminatedDate().getTime()));
        } else {
            pstmt.setNull(8, Types.DATE);
        }
        
        pstmt.setBigDecimal(9, contract.getDepositAmount());
        pstmt.setString(10, contract.getStatus());
        pstmt.setString(11, contract.getNotes());
        pstmt.setLong(12, contract.getId());
        
        boolean success = pstmt.executeUpdate() > 0;
        
        // LOG TO HISTORY - UPDATED (in separate transaction)
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
    }
    return false;
}
    
    // --- RENEW CONTRACT (UPDATED WITH LOGGING) ---
    public boolean renewContract(Long contractId, java.util.Date newEndDate) {
    // Get old end date first
    Contract contract = getContractById(contractId);
    if (contract == null) return false;
    
    java.util.Date oldEndDate = contract.getEndDate();
    
    String sql = "UPDATE contracts SET end_date = ? WHERE id = ?";
    
    try (Connection conn = Db_connection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setDate(1, new java.sql.Date(newEndDate.getTime()));
        pstmt.setLong(2, contractId);
        
        boolean success = pstmt.executeUpdate() > 0;
        
        // LOG TO HISTORY - RENEWED (in separate transaction)
        if (success) {
            try {
                ContractHistory history = new ContractHistory();
                history.setContractId(contractId);
                history.setAction("RENEWED");
                history.setOldEndDate(oldEndDate);
                history.setNewEndDate(newEndDate);
                history.setReason("Gia hạn hợp đồng");
                history.setCreatedBy(getCurrentUserId());
                contractHistoryDAO.insert(history);  // ← Dùng method không cần conn
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
    
    // --- TERMINATE CONTRACT (UPDATED WITH LOGGING) ---
    public boolean terminateContract(Long contractId, String reason) {
    String sqlUpdate = "UPDATE contracts SET " +
                      "status = 'TERMINATED', " +
                      "terminated_date = CURDATE(), " +
                      "notes = CONCAT(IFNULL(notes, ''), '\n[Kết thúc] ', ?) " +
                      "WHERE id = ?";
    String sqlResetApartment = "UPDATE apartments a " +
                              "INNER JOIN contracts c ON a.id = c.apartment_id " +
                              "SET a.status = 'AVAILABLE' " +
                              "WHERE c.id = ?";
    
    // ✅ try-with-resources tự động đóng connection
    try (Connection conn = Db_connection.getConnection()) {
        conn.setAutoCommit(false);
        
        try {
            // 1. Update contract
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setString(1, reason);
                pstmt.setLong(2, contractId);
                pstmt.executeUpdate();
            }
            
            // 2. Reset apartment status
            try (PreparedStatement pstmt = conn.prepareStatement(sqlResetApartment)) {
                pstmt.setLong(1, contractId);
                pstmt.executeUpdate();
            }
            
            // 3. LOG TO HISTORY - Dùng CONNECTION CÓ SẴN
            ContractHistory history = new ContractHistory();
            history.setContractId(contractId);
            history.setAction("TERMINATED");
            history.setReason(reason);
            history.setCreatedBy(getCurrentUserId());
            contractHistoryDAO.insert(conn, history);  // ← DÙNG OVERLOADED METHOD
            
            conn.commit();
            System.out.println("✅ Contract terminated successfully!");
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("❌ Transaction rolled back in terminateContract()");
            throw e;
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


    // --- DELETE CONTRACT + AUTO RESET APARTMENT (TRANSACTION) ---
    public boolean deleteContract(Long contractId) {
    String sqlGetAptId = "SELECT apartment_id FROM contracts WHERE id = ?";
    String sqlDeleteContract = "UPDATE contracts SET is_deleted = 1 WHERE id = ?";
    String sqlResetApartment = "UPDATE apartments SET status = 'AVAILABLE' WHERE id = ?";

    // ✅ XÓA dòng: Connection conn = null;
    try (Connection conn = Db_connection.getConnection()) {
        conn.setAutoCommit(false);
        
        try {
            // 1. Get apartment ID
            Long apartmentId = null;
            try (PreparedStatement pstG = conn.prepareStatement(sqlGetAptId)) {
                pstG.setLong(1, contractId);
                try (ResultSet rs = pstG.executeQuery()) {
                    if (rs.next()) apartmentId = rs.getLong("apartment_id");
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
            
            // 4. LOG TO HISTORY - Dùng connection có sẵn
            ContractHistory history = new ContractHistory();
            history.setContractId(contractId);
            history.setAction("DELETED");
            history.setReason("Xóa hợp đồng");
            history.setCreatedBy(getCurrentUserId());
            contractHistoryDAO.insert(conn, history);  // ← DÙNG OVERLOADED METHOD

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
    
    // --- COUNT CONTRACTS BY STATUS ---
    public int countContractsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM contracts WHERE status = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
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

    // --- COUNT ACTIVE CONTRACTS ---
    public int countActiveContracts() {
        return countContractsByStatus("ACTIVE");
    }
    
    // --- GET EXPIRING CONTRACTS (WITHIN N DAYS) ---
    public List<Contract> getExpiringContracts(int daysThreshold) {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts " +
                     "WHERE end_date IS NOT NULL " +
                     "AND DATEDIFF(end_date, CURDATE()) BETWEEN 0 AND ? " +
                     "AND status != 'TERMINATED' " +
                     "AND is_deleted = 0 " +
                     "ORDER BY end_date ASC";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    
    // --- COUNT INVOICES BY CONTRACT (FOR VALIDATION) ---
    public int countInvoicesByContract(Long contractId) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE contract_id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    
    public String generateContractNumber() {
    String sql = "SELECT IFNULL(MAX(CAST(SUBSTRING(contract_number, 12, 3) AS UNSIGNED)), 0) + 1 AS next_seq " +
                 "FROM contracts " +
                 "WHERE contract_number LIKE CONCAT('HD', DATE_FORMAT(CURDATE(), '%Y%m%d'), '%')";
    
    try (Connection conn = Db_connection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
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
}