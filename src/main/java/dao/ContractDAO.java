package dao;

import model.Contract;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Contract operations
 */
public class ContractDAO {
    
    // Get all contracts
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts WHERE is_deleted = 0 ORDER BY start_date DESC";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Contract contract = new Contract();
                contract.setId(rs.getLong("id"));
                contract.setApartmentId(rs.getLong("apartment_id"));
                contract.setResidentId(rs.getLong("resident_id"));
                // Fix: Convert java.sql.Date to java.util.Date
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
                contracts.add(contract);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }
    
    // Get contract by ID
    public Contract getContractById(Long id) {
        String sql = "SELECT * FROM contracts WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Contract contract = new Contract();
                contract.setId(rs.getLong("id"));
                contract.setApartmentId(rs.getLong("apartment_id"));
                contract.setResidentId(rs.getLong("resident_id"));
                // Fix: Convert java.sql.Date to java.util.Date
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get active contracts
    public List<Contract> getActiveContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts WHERE status = 'ACTIVE' AND is_deleted = 0 ORDER BY start_date DESC";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Contract contract = new Contract();
                contract.setId(rs.getLong("id"));
                contract.setApartmentId(rs.getLong("apartment_id"));
                contract.setResidentId(rs.getLong("resident_id"));
                // Fix: Convert java.sql.Date to java.util.Date
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
                contracts.add(contract);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }
    
    // Insert new contract
    public boolean insertContract(Contract contract) {
        String sql = "INSERT INTO contracts (apartment_id, resident_id, start_date, end_date, deposit_amount, status, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, contract.getApartmentId());
            pstmt.setLong(2, contract.getResidentId());
            pstmt.setDate(3, new java.sql.Date(contract.getStartDate().getTime()));
            if (contract.getEndDate() != null) {
                pstmt.setDate(4, new java.sql.Date(contract.getEndDate().getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setBigDecimal(5, contract.getDepositAmount());
            pstmt.setString(6, contract.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update contract
    public boolean updateContract(Contract contract) {
        String sql = "UPDATE contracts SET apartment_id = ?, resident_id = ?, start_date = ?, " +
                     "end_date = ?, deposit_amount = ?, status = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, contract.getApartmentId());
            pstmt.setLong(2, contract.getResidentId());
            pstmt.setDate(3, new java.sql.Date(contract.getStartDate().getTime()));
            if (contract.getEndDate() != null) {
                pstmt.setDate(4, new java.sql.Date(contract.getEndDate().getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setBigDecimal(5, contract.getDepositAmount());
            pstmt.setString(6, contract.getStatus());
            pstmt.setLong(7, contract.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete contract
    public boolean deleteContract(Long id) {
        String sql = "UPDATE contracts SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Count active contracts
    public int countActiveContracts() {
        String sql = "SELECT COUNT(*) FROM contracts WHERE status = 'ACTIVE' AND is_deleted = 0";
        
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
}