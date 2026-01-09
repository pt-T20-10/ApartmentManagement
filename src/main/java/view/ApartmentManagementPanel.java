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
import java.math.BigDecimal;
import java.util.List;

/**
 * Apartment Management Panel
 * Full CRUD operations for apartments
 */
public class ApartmentManagementPanel extends JPanel {
    
    private ApartmentDAO apartmentDAO;
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JTable apartmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Form fields
    private JComboBox<Building> buildingCombo;
    private JComboBox<Floor> floorCombo;
    private JTextField apartmentNumberField;
    private JTextField areaField;
    private JSpinner bedroomsSpinner;
    private JComboBox<String> statusCombo;
    
    private Apartment selectedApartment = null;
    
    public ApartmentManagementPanel() {
        this.apartmentDAO = new ApartmentDAO();
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createFormPanel();
        
        loadBuildings();
        loadApartments();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[A]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Can Ho");
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
        searchButton.addActionListener(e -> searchApartments());
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
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
        String[] columns = {"ID", "Toa Nha", "Tang", "So Can Ho", "Dien Tich (m2)", "Phong Ngu", "Trang Thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        apartmentTable = new JTable(tableModel);
        apartmentTable.setFont(UIConstants.FONT_REGULAR);
        apartmentTable.setRowHeight(40);
        apartmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apartmentTable.setShowGrid(true);
        apartmentTable.setGridColor(UIConstants.BORDER_COLOR);
        apartmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedApartment();
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
        apartmentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        apartmentTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        apartmentTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(apartmentTable);
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
        JLabel formTitle = new JLabel("Thong Tin Can Ho");
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
        buildingCombo.addActionListener(e -> onBuildingSelected());
        formPanel.add(buildingCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Floor field
        formPanel.add(createFieldLabel("Tang *"));
        floorCombo = new JComboBox<>();
        floorCombo.setFont(UIConstants.FONT_REGULAR);
        floorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        floorCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(floorCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Apartment number field
        formPanel.add(createFieldLabel("So Can Ho *"));
        apartmentNumberField = createTextField();
        formPanel.add(apartmentNumberField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Area field
        formPanel.add(createFieldLabel("Dien Tich (m2) *"));
        areaField = createTextField();
        formPanel.add(areaField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Bedrooms spinner
        formPanel.add(createFieldLabel("So Phong Ngu *"));
        bedroomsSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        bedroomsSpinner.setFont(UIConstants.FONT_REGULAR);
        bedroomsSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bedroomsSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(bedroomsSpinner);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Status field
        formPanel.add(createFieldLabel("Trang Thai *"));
        statusCombo = new JComboBox<>(new String[]{"Available", "Occupied", "Maintenance"});
        statusCombo.setFont(UIConstants.FONT_REGULAR);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(statusCombo);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton addButton = new ModernButton("[+] Them", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addApartment());
        
        ModernButton updateButton = new ModernButton("[E] Sua", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateApartment());
        
        ModernButton deleteButton = new ModernButton("[X] Xoa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteApartment());
        
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
        
        if (buildingCombo.getItemCount() > 0) {
            onBuildingSelected();
        }
    }
    
    private void onBuildingSelected() {
        Building selectedBuilding = (Building) buildingCombo.getSelectedItem();
        if (selectedBuilding == null) {
            floorCombo.setModel(new DefaultComboBoxModel<>());
            return;
        }
        
        DefaultComboBoxModel<Floor> model = new DefaultComboBoxModel<>();
        List<Floor> floors = floorDAO.getFloorsByBuildingId(selectedBuilding.getId());
        
        for (Floor floor : floors) {
            model.addElement(floor);
        }
        
        floorCombo.setModel(model);
    }
    
    private void loadApartments() {
        tableModel.setRowCount(0);
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        
        for (Apartment apartment : apartments) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            String floorInfo = "N/A";
            String buildingName = "N/A";
            
            if (floor != null) {
                floorInfo = "Tang " + floor.getFloorNumber();
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
    
    private void loadSelectedApartment() {
        int selectedRow = apartmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedApartment = apartmentDAO.getApartmentById(id);
            
            if (selectedApartment != null) {
                Floor floor = floorDAO.getFloorById(selectedApartment.getFloorId());
                if (floor != null) {
                    Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                    if (building != null) {
                        buildingCombo.setSelectedItem(building);
                    }
                    floorCombo.setSelectedItem(floor);
                }
                
                apartmentNumberField.setText(selectedApartment.getApartmentNumber());
                areaField.setText(selectedApartment.getArea().toString());
                bedroomsSpinner.setValue(selectedApartment.getBedrooms());
                statusCombo.setSelectedItem(selectedApartment.getStatus());
            }
        }
    }
    
    private void addApartment() {
        if (!validateForm()) {
            return;
        }
        
        Floor selectedFloor = (Floor) floorCombo.getSelectedItem();
        if (selectedFloor == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon tang!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Apartment apartment = new Apartment();
        apartment.setFloorId(selectedFloor.getId());
        apartment.setApartmentNumber(apartmentNumberField.getText().trim());
        apartment.setArea(new BigDecimal(areaField.getText().trim()));
        apartment.setBedrooms((Integer) bedroomsSpinner.getValue());
        apartment.setStatus((String) statusCombo.getSelectedItem());
        
        if (apartmentDAO.insertApartment(apartment)) {
            JOptionPane.showMessageDialog(this, 
                "Them can ho thanh cong!", 
                "Thanh Cong", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadApartments();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Them can ho that bai!", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateApartment() {
        if (selectedApartment == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon can ho can sua!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        Floor selectedFloor = (Floor) floorCombo.getSelectedItem();
        if (selectedFloor == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon tang!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        selectedApartment.setFloorId(selectedFloor.getId());
        selectedApartment.setApartmentNumber(apartmentNumberField.getText().trim());
        selectedApartment.setArea(new BigDecimal(areaField.getText().trim()));
        selectedApartment.setBedrooms((Integer) bedroomsSpinner.getValue());
        selectedApartment.setStatus((String) statusCombo.getSelectedItem());
        
        if (apartmentDAO.updateApartment(selectedApartment)) {
            JOptionPane.showMessageDialog(this, 
                "Cap nhat can ho thanh cong!", 
                "Thanh Cong", 
                JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadApartments();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cap nhat can ho that bai!", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteApartment() {
        if (selectedApartment == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon can ho can xoa!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa can ho " + selectedApartment.getApartmentNumber() + "?",
            "Xac Nhan Xoa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (apartmentDAO.deleteApartment(selectedApartment.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Xoa can ho thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadApartments();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xoa can ho that bai!", 
                    "Loi", 
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
                    floorInfo = "Tang " + floor.getFloorNumber();
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
                "Khong tim thay can ho nao!", 
                "Thong Bao", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearForm() {
        apartmentNumberField.setText("");
        areaField.setText("");
        bedroomsSpinner.setValue(1);
        statusCombo.setSelectedIndex(0);
        selectedApartment = null;
        apartmentTable.clearSelection();
        
        if (buildingCombo.getItemCount() > 0) {
            buildingCombo.setSelectedIndex(0);
        }
    }
    
    private boolean validateForm() {
        if (apartmentNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap so can ho!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            apartmentNumberField.requestFocus();
            return false;
        }
        
        if (areaField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap dien tich!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            areaField.requestFocus();
            return false;
        }
        
        try {
            BigDecimal area = new BigDecimal(areaField.getText().trim());
            if (area.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Dien tich phai lon hon 0!", 
                    "Canh Bao", 
                    JOptionPane.WARNING_MESSAGE);
                areaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Dien tich khong hop le!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            areaField.requestFocus();
            return false;
        }
        
        return true;
    }
}