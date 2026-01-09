package dao;

import model.Service;
import connection.Db_connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Service operations
 */
public class ServiceDAO {
    
    // Get all services
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE is_deleted = 0 ORDER BY service_name";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getLong("id"));
                service.setServiceName(rs.getString("service_name"));
                service.setUnitPrice(rs.getBigDecimal("unit_price"));
                service.setUnitType(rs.getString("unit_type"));
                service.setMandatory(rs.getBoolean("is_mandatory"));
                service.setDeleted(rs.getBoolean("is_deleted"));
                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }
    
    // Get service by ID
    public Service getServiceById(Long id) {
        String sql = "SELECT * FROM services WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Service service = new Service();
                service.setId(rs.getLong("id"));
                service.setServiceName(rs.getString("service_name"));
                service.setUnitPrice(rs.getBigDecimal("unit_price"));
                service.setUnitType(rs.getString("unit_type"));
                service.setMandatory(rs.getBoolean("is_mandatory"));
                service.setDeleted(rs.getBoolean("is_deleted"));
                return service;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get mandatory services
    public List<Service> getMandatoryServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE is_mandatory = 1 AND is_deleted = 0 ORDER BY service_name";
        
        try (Connection conn = Db_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getLong("id"));
                service.setServiceName(rs.getString("service_name"));
                service.setUnitPrice(rs.getBigDecimal("unit_price"));
                service.setUnitType(rs.getString("unit_type"));
                service.setMandatory(rs.getBoolean("is_mandatory"));
                service.setDeleted(rs.getBoolean("is_deleted"));
                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }
    
    // Insert new service
    public boolean insertService(Service service) {
        String sql = "INSERT INTO services (service_name, unit_price, unit_type, is_mandatory, is_deleted) " +
                     "VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, service.getServiceName());
            pstmt.setBigDecimal(2, service.getUnitPrice());
            pstmt.setString(3, service.getUnitType());
            pstmt.setBoolean(4, service.isMandatory());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update service
    public boolean updateService(Service service) {
        String sql = "UPDATE services SET service_name = ?, unit_price = ?, unit_type = ?, is_mandatory = ? WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, service.getServiceName());
            pstmt.setBigDecimal(2, service.getUnitPrice());
            pstmt.setString(3, service.getUnitType());
            pstmt.setBoolean(4, service.isMandatory());
            pstmt.setLong(5, service.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Soft delete service
    public boolean deleteService(Long id) {
        String sql = "UPDATE services SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = Db_connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}