package view;

import dao.BuildingDAO;
import model.Building;
import util.UIConstants;
import util.ModernButton;
import util.PermissionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Building Management Panel with RBAC
 * Permissions:
 * - ADMIN: Full CRUD
 * - STAFF: View only
 * - ACCOUNTANT: View only
 */
public class BuildingManagementPanel extends JPanel {
    
    private BuildingDAO buildingDAO;
    private PermissionManager permissionManager;
    private JTable buildingTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Action buttons (stored as fields for RBAC control)
    private ModernButton addButton;
    private ModernButton editButton;
    private ModernButton deleteButton;
    
    public BuildingManagementPanel() {
        this.buildingDAO = new BuildingDAO();
        this.permissionManager = PermissionManager.getInstance();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        applyPermissions(); // Apply RBAC
        
        loadBuildings();
    }
    
    /**
     * Apply role-based permissions to UI elements
     */
    private void applyPermissions() {
        boolean canAdd = permissionManager.canAdd(PermissionManager.MODULE_BUILDINGS);
        boolean canEdit = permissionManager.canEdit(PermissionManager.MODULE_BUILDINGS);
        boolean canDelete = permissionManager.canDelete(PermissionManager.MODULE_BUILDINGS);
        
        // Hide/disable buttons based on permissions
        if (addButton != null) addButton.setVisible(canAdd);
        if (editButton != null) editButton.setVisible(canEdit);
        if (deleteButton != null) deleteButton.setVisible(canDelete);
        
        // Disable double-click edit if no edit permission
        if (!canEdit) {
            // Remove mouse listener for double-click edit
            java.awt.event.MouseListener[] listeners = buildingTable.getMouseListeners();
            for (java.awt.event.MouseListener listener : listeners) {
                buildingTable.removeMouseListener(listener);
            }
        }
        
        // Show permission indicator
        showPermissionIndicator();
    }
    
    /**
     * Show permission level indicator in header
     */
    private void showPermissionIndicator() {
        String permission = permissionManager.getPermissionDescription(PermissionManager.MODULE_BUILDINGS);
        
        if (!permissionManager.isAdmin()) {
            // Add a subtle permission indicator
            // This is already shown in the title area, so we can skip or add a tooltip
        }
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("🏗️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Tòa Nhà");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Add permission indicator
        String permission = permissionManager.getPermissionDescription(PermissionManager.MODULE_BUILDINGS);
        if (!permissionManager.isAdmin()) {
            JLabel permLabel = new JLabel("(" + permission + ")");
            permLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            permLabel.setForeground(UIConstants.TEXT_SECONDARY);
            titlePanel.add(Box.createHorizontalStrut(10));
            titlePanel.add(permLabel);
        }
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.addActionListener(e -> searchBuildings());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchBuildings());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBuildings();
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
        String[] columns = {"ID", "Tên Tòa Nhà", "Địa Chỉ", "Người Quản Lý", "Mô Tả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        buildingTable = new JTable(tableModel);
        buildingTable.setFont(UIConstants.FONT_REGULAR);
        buildingTable.setRowHeight(45);
        buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingTable.setShowGrid(true);
        buildingTable.setGridColor(UIConstants.BORDER_COLOR);
        
        // Double-click to edit (will be removed if no edit permission)
        buildingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editBuilding();
                }
            }
        });
        
        // Table header
        JTableHeader header = buildingTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Column widths
        buildingTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        buildingTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        buildingTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        buildingTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        buildingTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        addButton = new ModernButton("➕ Thêm Tòa Nhà", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addBuilding());
        
        editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editBuilding());
        
        deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteBuilding());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadBuildings() {
        tableModel.setRowCount(0);
        List<Building> buildings = buildingDAO.getAllBuildings();
        
        for (Building building : buildings) {
            Object[] row = {
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getManagerName() != null ? building.getManagerName() : "",
                building.getDescription() != null ? building.getDescription() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void addBuilding() {
        // Check permission
        if (!permissionManager.canAdd(PermissionManager.MODULE_BUILDINGS)) {
            permissionManager.showAccessDeniedMessage(this, "thêm tòa nhà");
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog
        BuildingDialog dialog = new BuildingDialog(parentFrame);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Building building = dialog.getBuilding();
            
            if (buildingDAO.addBuilding(building)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm tòa nhà thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm tòa nhà thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editBuilding() {
        // Check permission
        if (!permissionManager.canEdit(PermissionManager.MODULE_BUILDINGS)) {
            permissionManager.showAccessDeniedMessage(this, "sửa tòa nhà");
            return;
        }
        
        int selectedRow = buildingTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tòa nhà cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Building building = buildingDAO.getBuildingById(id);
        
        if (building == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy tòa nhà!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog with existing building
        BuildingDialog dialog = new BuildingDialog(parentFrame, building);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Building updatedBuilding = dialog.getBuilding();
            
            if (buildingDAO.updateBuilding(updatedBuilding)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật tòa nhà thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật tòa nhà thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteBuilding() {
        // Check permission
        if (!permissionManager.canDelete(PermissionManager.MODULE_BUILDINGS)) {
            permissionManager.showAccessDeniedMessage(this, "xóa tòa nhà");
            return;
        }
        
        int selectedRow = buildingTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tòa nhà cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tòa nhà '" + name + "'?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (buildingDAO.deleteBuilding(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa tòa nhà thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa tòa nhà thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchBuildings() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadBuildings();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Building> buildings = buildingDAO.searchBuildingsByName(keyword);
        
        for (Building building : buildings) {
            Object[] row = {
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getManagerName() != null ? building.getManagerName() : "",
                building.getDescription() != null ? building.getDescription() : ""
            };
            tableModel.addRow(row);
        }
        
        if (buildings.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy tòa nhà nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}