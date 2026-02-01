package dao;

import model.Invoice;
import connection.Db_connection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.InvoiceDetail;

/**
 * DAO class for Invoice operations
 */
public class InvoiceDAO {

    // Get all invoices
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE is_deleted = 0 AND status <> 'CANCELED' ORDER BY year DESC, month DESC";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

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

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    // ===== ADDED FOR PANEL COMPATIBILITY =====
    /**
     * Get invoices by month and year
     */
    public List<Invoice> getInvoicesByMonth(int month, int year) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE month = ? AND year = ? AND is_deleted = 0 ORDER BY id DESC";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

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

    // Get unpaid invoices
    public List<Invoice> getUnpaidInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE status = 'UNPAID' AND is_deleted = 0 ORDER BY year DESC, month DESC";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

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
    public Long insertInvoiceAndReturnId(Invoice invoice) {
        String sql = "INSERT INTO invoices "
                + "(contract_id, month, year, total_amount, status, created_at, is_deleted) "
                + "VALUES (?, ?, ?, ?, ?, NOW(), 0)";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt
                = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, invoice.getContractId());
            pstmt.setInt(2, invoice.getMonth());
            pstmt.setInt(3, invoice.getYear());
            pstmt.setBigDecimal(4, invoice.getTotalAmount());
            pstmt.setString(5, invoice.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                return null;
            }

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update invoice
    public boolean updateInvoice(Invoice invoice) {
        String sql = "UPDATE invoices SET contract_id = ?, month = ?, year = ?, total_amount = ?, "
                + "status = ?, payment_date = ? WHERE id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    // Count unpaid invoices
    public int countUnpaidInvoices() {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = 'UNPAID' AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

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

        try (Connection conn = Db_connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

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

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public Invoice getLatestInvoiceByApartmentId(Long apartmentId) {
        // Query này Join với bảng contracts để tìm hóa đơn của căn hộ đó
        // Giả định bảng contracts có cột id và apartment_id
        String sql = "SELECT i.* FROM invoices i "
                + "JOIN contracts c ON i.contract_id = c.id "
                + "WHERE c.apartment_id = ? AND i.is_deleted = 0 "
                + "ORDER BY i.year DESC, i.month DESC, i.id DESC LIMIT 1";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, apartmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setId(rs.getLong("id"));
                invoice.setContractId(rs.getLong("contract_id"));
                invoice.setMonth(rs.getInt("month"));
                invoice.setYear(rs.getInt("year"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setStatus(rs.getString("status"));

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

    public boolean insertInvoiceDetails(Long invoiceId, List<InvoiceDetail> details) {
        if (details == null || details.isEmpty()) {
            return true;
        }

        String sql = "INSERT INTO invoice_details "
                + "(invoice_id, service_name, unit_price, quantity, amount) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (InvoiceDetail d : details) {
                pstmt.setLong(1, invoiceId);
                pstmt.setString(2, d.getServiceName());
                pstmt.setBigDecimal(3, d.getUnitPrice());
                pstmt.setDouble(4, d.getQuantity());
                pstmt.setBigDecimal(5, d.getAmount());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách chi tiết dịch vụ của hóa đơn
     */
    public List<InvoiceDetail> getInvoiceDetails(Long invoiceId) {
        List<InvoiceDetail> details = new ArrayList<>();
        // Giả định bảng chi tiết tên là invoice_details
        String sql = "SELECT * FROM invoice_details WHERE invoice_id = ?";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                InvoiceDetail detail = new InvoiceDetail();
                detail.setId(rs.getLong("id"));
                detail.setInvoiceId(rs.getLong("invoice_id"));
                detail.setServiceName(rs.getString("service_name"));
                detail.setUnitPrice(rs.getBigDecimal("unit_price"));
                detail.setQuantity(rs.getDouble("quantity"));
                detail.setAmount(rs.getBigDecimal("amount"));
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public BigDecimal getRevenueByYear(int year) {
        String sql = "SELECT SUM(total_amount) FROM invoices "
                + "WHERE year = ? AND status = 'PAID' AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int countUnpaidInvoicesByMonth(int month, int year) {
        String sql = "SELECT COUNT(*) FROM invoices "
                + "WHERE status = 'UNPAID' AND month = ? AND year = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countUnpaidInvoicesByYear(int year) {
        String sql = "SELECT COUNT(*) FROM invoices "
                + "WHERE status = 'UNPAID' AND year = ? AND is_deleted = 0";

        try (Connection conn = Db_connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
