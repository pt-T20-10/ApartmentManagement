package model;

import java.math.BigDecimal;

/**
 * Apartment Entity
 * Represents an apartment/unit in a floor
 */
public class Apartment {
    private Long id;
    private Long floorId;
    private String roomNumber;
    private Double area;
    private String status; // AVAILABLE, RENTED, MAINTENANCE
    private BigDecimal basePrice;
    private String description;
    private boolean isDeleted;
    
    // For display purposes
    private String floorName;
    private String buildingName;
    
    // Constructors
    public Apartment() {
    }
    
    public Apartment(Long id, Long floorId, String roomNumber, Double area, String status, 
                     BigDecimal basePrice, String description, boolean isDeleted) {
        this.id = id;
        this.floorId = floorId;
        this.roomNumber = roomNumber;
        this.area = area;
        this.status = status;
        this.basePrice = basePrice;
        this.description = description;
        this.isDeleted = isDeleted;
    }
    
    public Apartment(Long floorId, String roomNumber, Double area, String status, BigDecimal basePrice) {
        this.floorId = floorId;
        this.roomNumber = roomNumber;
        this.area = area;
        this.status = status;
        this.basePrice = basePrice;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFloorId() {
        return floorId;
    }
    
    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public Double getArea() {
        return area;
    }
    
    public void setArea(Double area) {
        this.area = area;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public String getFloorName() {
        return floorName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }
    
    @Override
    public String toString() {
        return "Apartment{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", area=" + area +
                ", status='" + status + '\'' +
                ", basePrice=" + basePrice +
                '}';
    }
}