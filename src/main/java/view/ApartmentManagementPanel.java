package view;

import dao.ApartmentDAO;
import dao.FloorDAO;
import dao.BuildingDAO;
import model.Apartment;
import model.Floor;
import model.Building;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Apartment Management Panel
 * Full CRUD operations with popup dialog
 */
public class ApartmentManagementPanel extends JPanel {
    
    private ApartmentDAO apartmentDAO;
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JTable apartmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public ApartmentManagementPanel() {
        this.apartmentDAO = new ApartmentDAO();
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadApartments();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("🏠");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Căn Hộ");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.addActionListener(e -> searchApartments());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchApartments());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadApartments();
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        // Table model
        String[] columns = {"ID", "Tòa Nhà", "Tầng", "Số CH", "Diện Tích (m²)", "Phòng Ngủ", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        apartmentTable = new JTable(tableModel);
        apartmentTable.setFont(UIConstants.FONT_REGULAR);
        apartmentTable.setRowHeight(45);
        apartmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apartmentTable.setShowGrid(true);
        apartmentTable.setGridColor(UIConstants.BORDER_COLOR);
        
        // Double-click to edit
        apartmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editApartment();
                }
            }
        });
        
        // Table header
        JTableHeader header = apartmentTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Column widths
        apartmentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        apartmentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        apartmentTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        apartmentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        apartmentTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        apartmentTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        apartmentTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(apartmentTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Căn Hộ", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addApartment());
        
        ModernButton editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editApartment());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteApartment());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadApartments() {
        tableModel.setRowCount(0);
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        
        for (Apartment apartment : apartments) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            String floorInfo = "N/A";
            String buildingName = "N/A";
            
            if (floor != null) {
                floorInfo = "Tầng " + floor.getFloorNumber();
                Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                if (building != null) {
                    buildingName = building.getName();
                }
            }
            
            Object[] row = {
                apartment.getId(),
                buildingName,
                floorInfo,
                apartment.getApartmentNumber(),
                apartment.getArea(),
                apartment.getBedrooms(),
                apartment.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addApartment() {
        
     System.out.println("addApartment called!"); // DEBUG
    
    // Get parent frame
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    System.out.println("Parent frame: " + parentFrame); // DEBUG
    
    // Show dialog
    System.out.println("Creating dialog..."); // DEBUG
    ApartmentDialog dialog = new ApartmentDialog(parentFrame);
    System.out.println("Dialog created, showing..."); // DEBUG
    dialog.setVisible(true);
    System.out.println("Dialog shown!"); // DEBUG
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Apartment apartment = dialog.getApartment();
            
            if (apartmentDAO.insertApartment(apartment)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm căn hộ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadApartments();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm căn hộ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editApartment() {
        int selectedRow = apartmentTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn căn hộ cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Apartment apartment = apartmentDAO.getApartmentById(id);
        
        if (apartment == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy căn hộ!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog with existing apartment
        ApartmentDialog dialog = new ApartmentDialog(parentFrame, apartment);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Apartment updatedApartment = dialog.getApartment();
            
            if (apartmentDAO.updateApartment(updatedApartment)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật căn hộ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadApartments();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật căn hộ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteApartment() {
        int selectedRow = apartmentTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn căn hộ cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String apartmentNumber = (String) tableModel.getValueAt(selectedRow, 3);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa căn hộ " + apartmentNumber + "?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (apartmentDAO.deleteApartment(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa căn hộ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadApartments();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa căn hộ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchApartments() {
        String keyword = searchField.getText().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            loadApartments();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        
        for (Apartment apartment : apartments) {
            if (apartment.getApartmentNumber().toLowerCase().contains(keyword) ||
                apartment.getStatus().toLowerCase().contains(keyword)) {
                
                Floor floor = floorDAO.getFloorById(apartment.getFloorId());
                String floorInfo = "N/A";
                String buildingName = "N/A";
                
                if (floor != null) {
                    floorInfo = "Tầng " + floor.getFloorNumber();
                    Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                    if (building != null) {
                        buildingName = building.getName();
                    }
                }
                
                Object[] row = {
                    apartment.getId(),
                    buildingName,
                    floorInfo,
                    apartment.getApartmentNumber(),
                    apartment.getArea(),
                    apartment.getBedrooms(),
                    apartment.getStatus()
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy căn hộ nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}