package dao;

import model.ContractHistory;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for ContractHistory operations
 */
public class ContractHistoryDAO {
    
    // --- HELPER: Map ResultSet to ContractHistory ---
    private ContractHistory mapResultSetToHistory(ResultSet rs) throws SQLException {
        ContractHistory history = new ContractHistory();
        history.setId(rs.getLong("id"));
        history.setContractId(rs.getLong("contract_id"));
        history.setAction(rs.getString("action"));
        history.setOldValue(rs.getString("old_value"));
        history.setNewValue(rs.getString("new_value"));
        
        java.sql.Date oldEndDate = rs.getDate("old_end_date");
        if (oldEndDate != null) {
            history.setOldEndDate(new java.util.Date(oldEndDate.getTime()));
        }
        
        java.sql.Date newEndDate = rs.getDate("new_end_date");
        if (newEndDate != null) {
            history.setNewEndDate(new java.util.Date(newEndDate.getTime()));
        }
        
        history.setReason(rs.getString("reason"));
        history.setCreatedBy(rs.getLong("created_by"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            history.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        
        return history;
    }
    
    // --- GET HISTORY BY CONTRACT ---
    public List<ContractHistory> getHistoryByContract(Long contractId) {
        List<ContractHistory> histories = new ArrayList<>();
        String sql = "SELECT * FROM contract_history " +
                     "WHERE contract_id = ? " +
                     "ORDER BY created_at DESC";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, contractId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return histories;
    }
    
    // --- INSERT HISTORY ---
    public boolean insert(ContractHistory history) {
        String sql = "INSERT INTO contract_history " +
                     "(contract_id, action, old_value, new_value, old_end_date, new_end_date, " +
                     "reason, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, history.getContractId());
            pstmt.setString(2, history.getAction());
            pstmt.setString(3, history.getOldValue());
            pstmt.setString(4, history.getNewValue());
            
            if (history.getOldEndDate() != null) {
                pstmt.setDate(5, new java.sql.Date(history.getOldEndDate().getTime()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            
            if (history.getNewEndDate() != null) {
                pstmt.setDate(6, new java.sql.Date(history.getNewEndDate().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            pstmt.setString(7, history.getReason());
            
            if (history.getCreatedBy() != null) {
                pstmt.setLong(8, history.getCreatedBy());
            } else {
                pstmt.setNull(8, Types.BIGINT);
            }
            
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        history.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // --- COUNT HISTORY BY CONTRACT ---
    public int countByContract(Long contractId) {
        String sql = "SELECT COUNT(*) FROM contract_history WHERE contract_id = ?";
        
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