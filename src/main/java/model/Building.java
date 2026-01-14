package model;

import java.sql.Date;

public class Building {
    private Long id;
    private String name;
    private String address;
    private String managerName;
    private String description;
    
    private String status;          // Trạng thái
    private Date operationDate;     // Ngày hoạt động
    private boolean isDeleted;

    public Building() {
        this.status = "Đang hoạt động";
        this.operationDate = new Date(System.currentTimeMillis());
    }

    // Constructor đầy đủ (Không còn numFloors)
    public Building(Long id, String name, String address, String managerName, String description, 
                    String status, Date operationDate, boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.status = status;
        this.operationDate = operationDate;
        this.isDeleted = isDeleted;
    }

    // Constructor thêm mới
    public Building(String name, String address, String managerName, String description,
                    String status, Date operationDate) {
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.status = status;
        this.operationDate = operationDate;
        this.isDeleted = false;
    }

    // Getters & Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getOperationDate() { return operationDate; }
    public void setOperationDate(Date operationDate) { this.operationDate = operationDate; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    
    @Override
    public String toString() { return this.name; }
}