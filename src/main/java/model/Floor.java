package model;
/**
 * Floor Entity
 * Represents a floor in a building
 */
public class Floor {
    private Long id;
    private Long buildingId;
    private int floorNumber;
    private String name;
    private String description; // Added for compatibility
    private boolean isDeleted;
    
    // For display purposes
    private String buildingName;
    
    // Constructors
    public Floor() {
    }
    
    public Floor(Long id, Long buildingId, int floorNumber, String name, boolean isDeleted) {
        this.id = id;
        this.buildingId = buildingId;
        this.floorNumber = floorNumber;
        this.name = name;
        this.isDeleted = isDeleted;
    }
    
    public Floor(Long buildingId, int floorNumber, String name) {
        this.buildingId = buildingId;
        this.floorNumber = floorNumber;
        this.name = name;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBuildingId() {
        return buildingId;
    }
    
    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }
    
    public int getFloorNumber() {
        return floorNumber;
    }
    
    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    // ===== ADDED FOR PANEL COMPATIBILITY =====
    
    /**
     * Get floor description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set floor description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Floor{" +
                "id=" + id +
                ", buildingId=" + buildingId +
                ", floorNumber=" + floorNumber +
                ", name='" + name + '\'' +
                '}';
    }
}