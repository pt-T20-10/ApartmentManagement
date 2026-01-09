package model;

import java.math.BigDecimal;

/**
 * Service Entity
 * Represents a service (electricity, water, management fee, etc.)
 */
public class Service {
    private Long id;
    private String serviceName;
    private BigDecimal unitPrice;
    private String unitType; // KWH, KHOI, THANG, XE
    private boolean isMandatory;
    private boolean isDeleted;
    
    // Constructors
    public Service() {
    }
    
    public Service(Long id, String serviceName, BigDecimal unitPrice, String unitType,
                   boolean isMandatory, boolean isDeleted) {
        this.id = id;
        this.serviceName = serviceName;
        this.unitPrice = unitPrice;
        this.unitType = unitType;
        this.isMandatory = isMandatory;
        this.isDeleted = isDeleted;
    }
    
    public Service(String serviceName, BigDecimal unitPrice, String unitType, boolean isMandatory) {
        this.serviceName = serviceName;
        this.unitPrice = unitPrice;
        this.unitType = unitType;
        this.isMandatory = isMandatory;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public String getUnitType() {
        return unitType;
    }
    
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
    
    public boolean isMandatory() {
        return isMandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", unitPrice=" + unitPrice +
                ", unitType='" + unitType + '\'' +
                '}';
    }
}