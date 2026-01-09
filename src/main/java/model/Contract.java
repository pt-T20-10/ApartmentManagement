package model;
import java.math.BigDecimal;
import java.util.Date;
/**
 * Model class representing a Contract
 */
public class Contract {
    private Long id;
    private Long apartmentId;
    private Long residentId;
    private Date startDate;
    private Date endDate;
    private BigDecimal depositAmount;
    private BigDecimal monthlyRent; // Added for compatibility
    private String status; // ACTIVE, EXPIRED, TERMINATED
    private boolean isDeleted;
    
    // Constructors
    public Contract() {
    }
    
    public Contract(Long id, Long apartmentId, Long residentId, Date startDate, Date endDate, 
                   BigDecimal depositAmount, String status, boolean isDeleted) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.residentId = residentId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.depositAmount = depositAmount;
        this.status = status;
        this.isDeleted = isDeleted;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    // ===== ALIAS METHODS FOR PANEL COMPATIBILITY =====
    
    /**
     * Get monthly rent amount
     */
    public BigDecimal getMonthlyRent() {
        return this.monthlyRent;
    }
    
    /**
     * Set monthly rent amount
     */
    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    
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
}