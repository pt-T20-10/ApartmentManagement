package model;

/**
 * Building Entity
 * Represents a building in the apartment management system
 */
public class Building {
    private Long id;
    private String name;
    private String address;
    private String managerName;
    private String description;
    private boolean isDeleted;
    
    // Constructors
    public Building() {
    }
    
    public Building(Long id, String name, String address, String managerName, String description, boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.isDeleted = isDeleted;
    }
    
    public Building(String name, String address, String managerName, String description) {
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getManagerName() {
        return managerName;
    }
    
    public void setManagerName(String managerName) {
        this.managerName = managerName;
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
    
    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", managerName='" + managerName + '\'' +
                '}';
    }
}