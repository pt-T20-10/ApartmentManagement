package view;

import dao.FloorDAO;
import dao.BuildingDAO;
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
 * Floor Management Panel
 * Full CRUD operations with popup dialog
 */
public class FloorManagementPanel extends JPanel {
    
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JTable floorTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public FloorManagementPanel() {
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadFloors();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("🏢");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Tầng");
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
        searchField.addActionListener(e -> searchFloors());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchFloors());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadFloors();
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
        String[] columns = {"ID", "Tòa Nhà", "Số Tầng", "Mô Tả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        floorTable = new JTable(tableModel);
        floorTable.setFont(UIConstants.FONT_REGULAR);
        floorTable.setRowHeight(45);
        floorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        floorTable.setShowGrid(true);
        floorTable.setGridColor(UIConstants.BORDER_COLOR);
        
        // Double-click to edit
        floorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editFloor();
                }
            }
        });
        
        // Table header
        JTableHeader header = floorTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Column widths
        floorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        floorTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        floorTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        floorTable.getColumnModel().getColumn(3).setPreferredWidth(400);
        
        JScrollPane scrollPane = new JScrollPane(floorTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Tầng", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addFloor());
        
        ModernButton editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editFloor());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteFloor());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadFloors() {
        tableModel.setRowCount(0);
        List<Floor> floors = floorDAO.getAllFloors();
        
        for (Floor floor : floors) {
            Building building = buildingDAO.getBuildingById(floor.getBuildingId());
            String buildingName = (building != null) ? building.getName() : "N/A";
            
            Object[] row = {
                floor.getId(),
                buildingName,
                floor.getFloorNumber(),
                floor.getDescription() != null ? floor.getDescription() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void addFloor() {
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog
        FloorDialog dialog = new FloorDialog(parentFrame);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Floor floor = dialog.getFloor();
            
            if (floorDAO.insertFloor(floor)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm tầng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm tầng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editFloor() {
        int selectedRow = floorTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tầng cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Floor floor = floorDAO.getFloorById(id);
        
        if (floor == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy tầng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog with existing floor
        FloorDialog dialog = new FloorDialog(parentFrame, floor);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Floor updatedFloor = dialog.getFloor();
            
            if (floorDAO.updateFloor(updatedFloor)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật tầng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật tầng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteFloor() {
        int selectedRow = floorTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tầng cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String buildingName = (String) tableModel.getValueAt(selectedRow, 1);
        int floorNumber = (Integer) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tầng " + floorNumber + " của " + buildingName + "?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (floorDAO.deleteFloor(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa tầng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa tầng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchFloors() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadFloors();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Floor> floors = floorDAO.getAllFloors();
        
        for (Floor floor : floors) {
            Building building = buildingDAO.getBuildingById(floor.getBuildingId());
            String buildingName = (building != null) ? building.getName() : "";
            
            if (buildingName.toLowerCase().contains(keyword.toLowerCase()) ||
                String.valueOf(floor.getFloorNumber()).contains(keyword)) {
                
                Object[] row = {
                    floor.getId(),
                    buildingName,
                    floor.getFloorNumber(),
                    floor.getDescription() != null ? floor.getDescription() : ""
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy tầng nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}