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
 * Full CRUD operations for floors
 */
public class FloorManagementPanel extends JPanel {
    
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JTable floorTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Form fields
    private JComboBox<Building> buildingCombo;
    private JTextField floorNumberField;
    private JTextArea descriptionArea;
    
    private Floor selectedFloor = null;
    
    public FloorManagementPanel() {
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createFormPanel();
        
        loadBuildings();
        loadFloors();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[F]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Tang");
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
        
        ModernButton searchButton = new ModernButton("[T] Tim Kiem", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchFloors());
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
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
        String[] columns = {"ID", "Toa Nha", "So Tang", "Mo Ta"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        floorTable = new JTable(tableModel);
        floorTable.setFont(UIConstants.FONT_REGULAR);
        floorTable.setRowHeight(40);
        floorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        floorTable.setShowGrid(true);
        floorTable.setGridColor(UIConstants.BORDER_COLOR);
        floorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedFloor();
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
        floorTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        floorTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        floorTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(floorTable);
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
        JLabel formTitle = new JLabel("Thong Tin Tang");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Building field
        formPanel.add(createFieldLabel("Toa Nha *"));
        buildingCombo = new JComboBox<>();
        buildingCombo.setFont(UIConstants.FONT_REGULAR);
        buildingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        buildingCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(buildingCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Floor number field
        formPanel.add(createFieldLabel("So Tang *"));
        floorNumberField = createTextField();
        formPanel.add(floorNumberField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Description field
        formPanel.add(createFieldLabel("Mo Ta"));
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
        
        ModernButton addButton = new ModernButton("[+] Them", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addFloor());
        
        ModernButton updateButton = new ModernButton("[E] Sua", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateFloor());
        
        ModernButton deleteButton = new ModernButton("[X] Xoa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteFloor());
        
        ModernButton clearButton = new ModernButton("[R] Lam Moi", UIConstants.TEXT_SECONDARY);
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
        DefaultComboBoxModel<Building> model = new DefaultComboBoxModel<>();
        List<Building> buildings = buildingDAO.getAllBuildings();
        
        for (Building building : buildings) {
            model.addElement(building);
        }
        
        buildingCombo.setModel(model);
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
                floor.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void loadSelectedFloor() {
        int selectedRow = floorTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedFloor = floorDAO.getFloorById(id);
            
            if (selectedFloor != null) {
                // Select building in combo
                Building building = buildingDAO.getBuildingById(selectedFloor.getBuildingId());
                if (building != null) {
                    buildingCombo.setSelectedItem(building);
                }
                
                floorNumberField.setText(String.valueOf(selectedFloor.getFloorNumber()));
                descriptionArea.setText(selectedFloor.getDescription());
            }
        }
    }
    
    private void addFloor() {
        if (!validateForm()) {
            return;
        }
        
        Building selectedBuilding = (Building) buildingCombo.getSelectedItem();
        if (selectedBuilding == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon toa nha!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Floor floor = new Floor();
        floor.setBuildingId(selectedBuilding.getId());
        floor.setFloorNumber(Integer.parseInt(floorNumberField.getText().trim()));
        floor.setDescription(descriptionArea.getText().trim());
        
        if (floorDAO.insertFloor(floor)) {
            JOptionPane.showMessageDialog(this, 
                "Them tang thanh cong!", 
                "Thanh Cong", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadFloors();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Them tang that bai!", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateFloor() {
        if (selectedFloor == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon tang can sua!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        Building selectedBuilding = (Building) buildingCombo.getSelectedItem();
        if (selectedBuilding == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon toa nha!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        selectedFloor.setBuildingId(selectedBuilding.getId());
        selectedFloor.setFloorNumber(Integer.parseInt(floorNumberField.getText().trim()));
        selectedFloor.setDescription(descriptionArea.getText().trim());
        
        if (floorDAO.updateFloor(selectedFloor)) {
            JOptionPane.showMessageDialog(this, 
                "Cap nhat tang thanh cong!", 
                "Thanh Cong", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadFloors();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cap nhat tang that bai!", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteFloor() {
        if (selectedFloor == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon tang can xoa!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa tang " + selectedFloor.getFloorNumber() + "?",
            "Xac Nhan Xoa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (floorDAO.deleteFloor(selectedFloor.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Xoa tang thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xoa tang that bai!", 
                    "Loi", 
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
                    floor.getDescription()
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Khong tim thay tang nao!", 
                "Thong Bao", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearForm() {
        floorNumberField.setText("");
        descriptionArea.setText("");
        selectedFloor = null;
        floorTable.clearSelection();
        if (buildingCombo.getItemCount() > 0) {
            buildingCombo.setSelectedIndex(0);
        }
    }
    
    private boolean validateForm() {
        if (floorNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap so tang!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            floorNumberField.requestFocus();
            return false;
        }
        
        try {
            int floorNumber = Integer.parseInt(floorNumberField.getText().trim());
            if (floorNumber < 0) {
                JOptionPane.showMessageDialog(this, 
                    "So tang phai lon hon hoac bang 0!", 
                    "Canh Bao", 
                    JOptionPane.WARNING_MESSAGE);
                floorNumberField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "So tang phai la so nguyen!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            floorNumberField.requestFocus();
            return false;
        }
        
        return true;
    }
}