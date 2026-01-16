package model;

public class Building {
    private Long id;
    private String name;
    private String address;
    private String managerName;
    private String description;
    
    private String status; 
    private boolean isDeleted;

    // --- Constructor mặc định ---
    public Building() {
        this.status = "Đang hoạt động";
    }

    // --- Constructor đầy đủ (dùng khi load từ DB) ---
    public Building(Long id, String name, String address, String managerName, String description, 
                    String status, boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    // --- Constructor thêm mới (dùng khi tạo mới) ---
    public Building(String name, String address, String managerName, String description,
                    String status) {
        this.name = name;
        this.address = address;
        this.managerName = managerName;
        this.description = description;
        this.status = status;
        this.isDeleted = false;
    }

    // --- Getters & Setters ---
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    
    @Override
    public String toString() { return this.name; }
}