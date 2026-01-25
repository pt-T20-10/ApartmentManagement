package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Model class representing a Contract
 * Updated with new fields from database migration
 */
public class Contract {
    // Primary key
    private Long id;
    
    // NEW: Contract identification
    private String contractNumber;      // HD20260118001
    
    // Foreign keys
    private Long apartmentId;
    private Long residentId;
    
    // NEW: Contract type
    private String contractType;        // RENTAL, OWNERSHIP
    
    // NEW: Signed date
    private Date signedDate;
    
    // Date range
    private Date startDate;
    private Date endDate;
    
    // NEW: Early termination date
    private Date terminatedDate;
    
    // Financial
    private BigDecimal depositAmount;
    private BigDecimal monthlyRent;     // For compatibility with existing code
    
    // Status
    private String status;              // ACTIVE, EXPIRED, TERMINATED
    
    // NEW: Notes
    private String notes;
    
    // Flags
    private boolean isDeleted;
    
    // NEW: Audit fields
    private Date createdAt;
    private Date updatedAt;
    
    private String tenantName;
    private String tenantPhone;
    
    // ===== CONSTRUCTORS =====
    
    public Contract() {
        this.contractType = "RENTAL";   // Default
        this.status = "ACTIVE";
        this.isDeleted = false;
    }
    
    public Contract(Long id, String contractNumber, Long apartmentId, Long residentId, 
                   String contractType, Date signedDate, Date startDate, Date endDate, 
                   Date terminatedDate, BigDecimal depositAmount, String status, 
                   String notes, boolean isDeleted, Date createdAt, Date updatedAt) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.apartmentId = apartmentId;
        this.residentId = residentId;
        this.contractType = contractType;
        this.signedDate = signedDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.terminatedDate = terminatedDate;
        this.depositAmount = depositAmount;
        this.status = status;
        this.notes = notes;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContractNumber() {
        return contractNumber;
    }
    
    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }
    
    public Long getApartmentId() {
        return apartmentId;
    }
    
    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }
    
    public Long getResidentId() {
        return residentId;
    }
    
    public void setResidentId(Long residentId) {
        this.residentId = residentId;
    }
    
    public String getContractType() {
        return contractType;
    }
    
    public void setContractType(String contractType) {
        this.contractType = contractType;
    }
    
    public Date getSignedDate() {
        return signedDate;
    }
    
    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Date getTerminatedDate() {
        return terminatedDate;
    }
    
    public void setTerminatedDate(Date terminatedDate) {
        this.terminatedDate = terminatedDate;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }
    
    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    
    // --- [QUAN TRỌNG] Getters/Setters cho Tenant ---
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ===== COMPUTED FIELDS / HELPER METHODS =====
    
    /**
     * Get display status based on current date and contract dates
     * Returns: "Đang hiệu lực", "Sắp hết hạn", "Đã hết hạn", "Đã hủy"
     */
    public String getStatusDisplay() {
        if ("TERMINATED".equals(status)) {
            return "Đã hủy";
        }
        
        if (endDate == null) {
            return "Đang hiệu lực"; // No end date = indefinite
        }
        
        LocalDate today = LocalDate.now();
        LocalDate contractEndDate = new java.sql.Date(endDate.getTime()).toLocalDate();
        long daysLeft = ChronoUnit.DAYS.between(today, contractEndDate);
        
        if (daysLeft < 0) {
            return "Đã hết hạn";
        } else if (daysLeft <= 30) {
            return "Sắp hết hạn";
        } else {
            return "Đang hiệu lực";
        }
    }
    
    /**
     * Get number of days left until contract expires
     * Returns negative if expired, null if no end date
     */
    public Long getDaysLeft() {
        if (endDate == null) {
            return null; // Indefinite contract
        }
        
        LocalDate today = LocalDate.now();
        LocalDate contractEndDate = new java.sql.Date(endDate.getTime()).toLocalDate();
        return ChronoUnit.DAYS.between(today, contractEndDate);
    }
    
    /**
     * Check if contract is active (not terminated and not expired)
     */
    public boolean isActive() {
        if ("TERMINATED".equals(status)) {
            return false;
        }
        
        if (endDate == null) {
            return true; // Indefinite contract
        }
        
        Long daysLeft = getDaysLeft();
        return daysLeft != null && daysLeft >= 0;
    }
    
    /**
     * Get contract type display
     */
    public String getContractTypeDisplay() {
        if ("RENTAL".equals(contractType)) {
            return "Thuê";
        } else if ("OWNERSHIP".equals(contractType)) {
            return "Sở hữu";
        }
        return contractType;
    }
    
    // ===== ALIAS METHODS FOR PANEL COMPATIBILITY =====
    
    /**
     * Alias for getDepositAmount() - for panel compatibility
     */
    public BigDecimal getDeposit() {
        return this.depositAmount;
    }
    
    /**
     * Alias for setDepositAmount() - for panel compatibility
     */
    public void setDeposit(BigDecimal deposit) {
        this.depositAmount = deposit;
    }
    
    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber + '\'' +
                ", type='" + contractType + '\'' +
                ", status='" + status + '\'' +
                ", apartmentId=" + apartmentId +
                ", residentId=" + residentId +
                '}';
    }
}