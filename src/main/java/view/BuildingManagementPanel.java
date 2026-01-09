package view;

import dao.BuildingDAO;
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
 * Building Management Panel
 * Full CRUD operations for buildings with modern UI
 */
public class BuildingManagementPanel extends JPanel {
    
    private BuildingDAO buildingDAO;
    private JTable buildingTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Form fields
    private JTextField nameField;
    private JTextField addressField;
    private JTextField managerField;
    private JTextArea descriptionArea;
    
    private Building selectedBuilding = null;
    
    public BuildingManagementPanel() {
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createFormPanel();
        
        loadBuildings();
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
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
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
        String[] columns = {"ID", "Tên Tòa Nhà", "Địa Chỉ", "Người Quản Lý"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        buildingTable = new JTable(tableModel);
        buildingTable.setFont(UIConstants.FONT_REGULAR);
        buildingTable.setRowHeight(40);
        buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingTable.setShowGrid(true);
        buildingTable.setGridColor(UIConstants.BORDER_COLOR);
        buildingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBuilding();
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
        buildingTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        buildingTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        formContainer.setPreferredSize(new Dimension(400, 0));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Form title
        JLabel formTitle = new JLabel("Thông Tin Tòa Nhà");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Name field
        formPanel.add(createFieldLabel("Tên Tòa Nhà *"));
        nameField = createTextField();
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Address field
        formPanel.add(createFieldLabel("Địa Chỉ *"));
        addressField = createTextField();
        formPanel.add(addressField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Manager field
        formPanel.add(createFieldLabel("Người Quản Lý"));
        managerField = createTextField();
        formPanel.add(managerField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Description field
        formPanel.add(createFieldLabel("Mô Tả"));
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(UIConstants.FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        formPanel.add(descScrollPane);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton addButton = new ModernButton("➕ Thêm", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addBuilding());
        
        ModernButton updateButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateBuilding());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteBuilding());
        
        ModernButton clearButton = new ModernButton("🔄 Làm Mới", UIConstants.TEXT_SECONDARY);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        formPanel.add(buttonPanel);
        
        formContainer.add(formPanel, BorderLayout.NORTH);
        add(formContainer, BorderLayout.EAST);
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_REGULAR);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }
    
    private void loadBuildings() {
        tableModel.setRowCount(0);
        List<Building> buildings = buildingDAO.getAllBuildings();
        
        for (Building building : buildings) {
            Object[] row = {
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getManagerName()
            };
            tableModel.addRow(row);
        }
    }
    
    private void loadSelectedBuilding() {
        int selectedRow = buildingTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedBuilding = buildingDAO.getBuildingById(id);
            
            if (selectedBuilding != null) {
                nameField.setText(selectedBuilding.getName());
                addressField.setText(selectedBuilding.getAddress());
                managerField.setText(selectedBuilding.getManagerName());
                descriptionArea.setText(selectedBuilding.getDescription());
            }
        }
    }
    
    private void addBuilding() {
        if (!validateForm()) {
            return;
        }
        
        Building building = new Building(
            nameField.getText().trim(),
            addressField.getText().trim(),
            managerField.getText().trim(),
            descriptionArea.getText().trim()
        );
        
        if (buildingDAO.addBuilding(building)) {
            JOptionPane.showMessageDialog(this, 
                "Thêm tòa nhà thành công!", 
                "Thành Công", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadBuildings();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Thêm tòa nhà thất bại!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateBuilding() {
        if (selectedBuilding == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tòa nhà cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        selectedBuilding.setName(nameField.getText().trim());
        selectedBuilding.setAddress(addressField.getText().trim());
        selectedBuilding.setManagerName(managerField.getText().trim());
        selectedBuilding.setDescription(descriptionArea.getText().trim());
        
        if (buildingDAO.updateBuilding(selectedBuilding)) {
            JOptionPane.showMessageDialog(this, 
                "Cập nhật tòa nhà thành công!", 
                "Thành Công", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadBuildings();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cập nhật tòa nhà thất bại!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteBuilding() {
        if (selectedBuilding == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn tòa nhà cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tòa nhà '" + selectedBuilding.getName() + "'?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (buildingDAO.deleteBuilding(selectedBuilding.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa tòa nhà thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
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
                building.getManagerName()
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
    
    private void clearForm() {
        nameField.setText("");
        addressField.setText("");
        managerField.setText("");
        descriptionArea.setText("");
        selectedBuilding = null;
        buildingTable.clearSelection();
    }
    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập tên tòa nhà!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập địa chỉ!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            addressField.requestFocus();
            return false;
        }
        
        return true;
    }
}