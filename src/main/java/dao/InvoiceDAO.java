package dao;

import model.Invoice;
import connection.Db_connection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Invoice operations
 */
public class InvoiceDAO {
    
    // Get all invoices
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE is_deleted = 0 ORDER BY year DESC, month DESC";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setId(rs.getLong("id"));
                invoice.setContractId(rs.getLong("contract_id"));
                invoice.setMonth(rs.getInt("month"));
                invoice.setYear(rs.getInt("year"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setStatus(rs.getString("status"));
                // Fix: Convert java.sql.Timestamp to java.util.Date
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    invoice.setCreatedAt(new java.util.Date(createdAt.getTime()));
                }
                Timestamp paymentDate = rs.getTimestamp("payment_date");
                if (paymentDate != null) {
                    invoice.setPaymentDate(new java.util.Date(paymentDate.getTime()));
                }
                invoice.setDeleted(rs.getBoolean("is_deleted"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    // Get invoice by ID
    public Invoice getInvoiceById(Long id) {
        String sql = "SELECT * FROM invoices WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setId(rs.getLong("id"));
                invoice.setContractId(rs.getLong("contract_id"));
                invoice.setMonth(rs.getInt("month"));
                invoice.setYear(rs.getInt("year"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setStatus(rs.getString("status"));
                // Fix: Convert java.sql.Timestamp to java.util.Date
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    invoice.setCreatedAt(new java.util.Date(createdAt.getTime()));
                }
                Timestamp paymentDate = rs.getTimestamp("payment_date");
                if (paymentDate != null) {
                    invoice.setPaymentDate(new java.util.Date(paymentDate.getTime()));
                }
                invoice.setDeleted(rs.getBoolean("is_deleted"));
                return invoice;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get unpaid invoices
    public List<Invoice> getUnpaidInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE status = 'UNPAID' AND is_deleted = 0 ORDER BY year DESC, month DESC";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setId(rs.getLong("id"));
                invoice.setContractId(rs.getLong("contract_id"));
                invoice.setMonth(rs.getInt("month"));
                invoice.setYear(rs.getInt("year"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setStatus(rs.getString("status"));
                // Fix: Convert java.sql.Timestamp to java.util.Date
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    invoice.setCreatedAt(new java.util.Date(createdAt.getTime()));
                }
                Timestamp paymentDate = rs.getTimestamp("payment_date");
                if (paymentDate != null) {
                    invoice.setPaymentDate(new java.util.Date(paymentDate.getTime()));
                }
                invoice.setDeleted(rs.getBoolean("is_deleted"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    // Insert new invoice
    public boolean insertInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (contract_id, month, year, total_amount, status, created_at, payment_date, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, invoice.getContractId());
            pstmt.setInt(2, invoice.getMonth());
            pstmt.setInt(3, invoice.getYear());
            pstmt.setBigDecimal(4, invoice.getTotalAmount());
            pstmt.setString(5, invoice.getStatus());
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            if (invoice.getPaymentDate() != null) {
                pstmt.setTimestamp(7, new Timestamp(invoice.getPaymentDate().getTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update invoice
    public boolean updateInvoice(Invoice invoice) {
        String sql = "UPDATE invoices SET contract_id = ?, month = ?, year = ?, total_amount = ?, " +
                     "status = ?, payment_date = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, invoice.getContractId());
            pstmt.setInt(2, invoice.getMonth());
            pstmt.setInt(3, invoice.getYear());
            pstmt.setBigDecimal(4, invoice.getTotalAmount());
            pstmt.setString(5, invoice.getStatus());
            if (invoice.getPaymentDate() != null) {
                pstmt.setTimestamp(6, new Timestamp(invoice.getPaymentDate().getTime()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }
            pstmt.setLong(7, invoice.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete invoice
    public boolean deleteInvoice(Long id) {
        String sql = "UPDATE invoices SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Count unpaid invoices
    public int countUnpaidInvoices() {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = 'UNPAID' AND is_deleted = 0";
        
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
    
    // Get total revenue (paid invoices)
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM invoices WHERE status = 'PAID' AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    // Get monthly revenue
    public BigDecimal getMonthlyRevenue(int month, int year) {
        String sql = "SELECT SUM(total_amount) FROM invoices WHERE month = ? AND year = ? AND status = 'PAID' AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}