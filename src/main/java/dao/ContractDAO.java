package dao;

import model.Contract;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Contract operations
 * UPDATED: Support for new database fields
 */
public class ContractDAO {

    // --- HELPER: Mapping ResultSet to Contract (UPDATED) ---
    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getLong("id"));
        contract.setApartmentId(rs.getLong("apartment_id"));
        contract.setResidentId(rs.getLong("resident_id"));
        
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) contract.setStartDate(new java.util.Date(startDate.getTime()));
        
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) contract.setEndDate(new java.util.Date(endDate.getTime()));
        
        contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        contract.setStatus(rs.getString("status"));
        contract.setDeleted(rs.getBoolean("is_deleted"));
        
        // Các trường này SQL chưa có, nên ta try-catch hoặc bỏ qua để tránh lỗi
        try { contract.setContractNumber(rs.getString("contract_number")); } catch (SQLException e) {}
        try { contract.setContractType(rs.getString("contract_type")); } catch (SQLException e) {}
        try { contract.setNotes(rs.getString("notes")); } catch (SQLException e) {}
        
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
    
    // --- NEW: GET CONTRACT BY CONTRACT NUMBER ---
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
    
    // --- NEW: GET CONTRACTS BY BUILDING ---
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
    
    // --- NEW: GET ACTIVE CONTRACTS BY APARTMENT ---
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
    
    // --- [MỚI] LẤY 1 HỢP ĐỒNG ACTIVE DUY NHẤT (CHO VIEW) ---
    public Contract getActiveContractByApartmentId(Long apartmentId) {
        String sql = 
            "SELECT c.*, r.full_name AS tenant_name, r.phone AS tenant_phone " +
            "FROM contracts c " +
            "JOIN residents r ON c.resident_id = r.id " +
            "WHERE c.apartment_id = ? " +
            "  AND c.status = 'ACTIVE' " +
            "  AND c.is_deleted = 0 " +
            "ORDER BY c.end_date DESC LIMIT 1";

        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, apartmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Contract c = mapResultSetToContract(rs);
                    // Map thêm thông tin người thuê
                    try {
                        c.setTenantName(rs.getString("tenant_name"));
                        c.setTenantPhone(rs.getString("tenant_phone"));
                    } catch (SQLException e) {
                        System.out.println("Lỗi map tenant info: " + e.getMessage());
                    }
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // --- NEW: CHECK IF APARTMENT HAS ACTIVE CONTRACT ---
    public boolean hasActiveContract(Long apartmentId) {
        List<Contract> activeContracts = getActiveContractsByApartment(apartmentId);
        return !activeContracts.isEmpty();
    }

    // --- INSERT CONTRACT (UPDATED WITH NEW FIELDS + TRANSACTION) ---
    public boolean insertContract(Contract contract) {
        String sqlContract = "INSERT INTO contracts " +
                           "(contract_number, apartment_id, resident_id, contract_type, " +
                           "signed_date, start_date, end_date, deposit_amount, status, notes, is_deleted) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        String sqlApartment = "UPDATE apartments SET status = 'RENTED' WHERE id = ?";

        Connection conn = null;
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert contract
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
                    // Get generated ID
                    try (ResultSet generatedKeys = pstC.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            contract.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            }

            // 2. Update apartment status to RENTED
            try (PreparedStatement pstA = conn.prepareStatement(sqlApartment)) {
                pstA.setLong(1, contract.getApartmentId());
                pstA.executeUpdate();
            }

            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { 
                    conn.rollback(); 
                } catch (SQLException ex) { 
                    ex.printStackTrace(); 
                }
            }
            e.printStackTrace();
        }
        return false;
    }

    // --- UPDATE CONTRACT (UPDATED WITH NEW FIELDS) ---
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
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // --- NEW: RENEW CONTRACT (GIA HẠN) ---
    public boolean renewContract(Long contractId, java.util.Date newEndDate) {
        String sql = "UPDATE contracts SET end_date = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(newEndDate.getTime()));
            pstmt.setLong(2, contractId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // --- NEW: TERMINATE CONTRACT (KẾT THÚC) ---
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
        
        Connection conn = null;
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false);
            
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

    // --- DELETE CONTRACT + AUTO RESET APARTMENT (TRANSACTION) ---
    public boolean deleteContract(Long contractId) {
        String sqlGetAptId = "SELECT apartment_id FROM contracts WHERE id = ?";
        String sqlDeleteContract = "UPDATE contracts SET is_deleted = 1 WHERE id = ?";
        String sqlResetApartment = "UPDATE apartments SET status = 'AVAILABLE' WHERE id = ?";

        Connection conn = null;
        try {
            conn = Db_connection.getConnection();
            conn.setAutoCommit(false);

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
    
    // --- NEW: GENERATE CONTRACT NUMBER ---
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
    
    // --- NEW: COUNT CONTRACTS BY STATUS ---
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
    
    // --- NEW: GET EXPIRING CONTRACTS (WITHIN N DAYS) ---
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
    
    // --- NEW: COUNT INVOICES BY CONTRACT (FOR VALIDATION) ---
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
}