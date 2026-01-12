package view;

import model.Apartment;
import model.Building;
import model.Floor;
import dao.BuildingDAO;
import dao.FloorDAO;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Apartment Dialog - Popup for Add/Edit Apartment
 * Features cascade dropdown: Building → Floor
 */
public class ApartmentDialog extends JDialog {
    
    private BuildingDAO buildingDAO;
    private FloorDAO floorDAO;
    
    private JComboBox<Building> buildingCombo;
    private JComboBox<Floor> floorCombo;
    private JTextField apartmentNumberField;
    private JTextField areaField;
    private JSpinner bedroomsSpinner;
    private JComboBox<String> statusCombo;
    
    private Apartment apartment;
    private boolean confirmed = false;
    
    /**
     * Constructor for Add mode
     */
    public ApartmentDialog(JFrame parent) {
        this(parent, null);
    }
    
    /**
     * Constructor for Edit mode
     */
    public ApartmentDialog(JFrame parent, Apartment apartment) {
        super(parent, apartment == null ? "Thêm Căn Hộ" : "Sửa Căn Hộ", true);
        this.apartment = apartment;
        this.buildingDAO = new BuildingDAO();
        this.floorDAO = new FloorDAO();
        
        initializeDialog();
        createContent();
        
        if (apartment != null) {
            loadApartmentData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        setSize(550, 650);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
    }
    
    private void createContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        // Form
        mainPanel.add(createForm(), BorderLayout.CENTER);
        
        // Buttons
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel(apartment == null ? "➕" : "✏️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(apartment == null ? "Thêm Căn Hộ Mới" : "Sửa Thông Tin Căn Hộ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    private JPanel createForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        // Building combo
        formPanel.add(createFieldLabel("Tòa Nhà *"));
        buildingCombo = new JComboBox<>();
        buildingCombo.setFont(UIConstants.FONT_REGULAR);
        buildingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        buildingCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildingCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Building) {
                    setText(((Building) value).getName());
                }
                return this;
            }
        });
        buildingCombo.addActionListener(e -> onBuildingSelected());
        formPanel.add(buildingCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Floor combo (cascade from building) - MUST create BEFORE loadBuildings()
        formPanel.add(createFieldLabel("Tầng *"));
        floorCombo = new JComboBox<>();
        floorCombo.setFont(UIConstants.FONT_REGULAR);
        floorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        floorCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        floorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Floor) {
                    setText("Tầng " + ((Floor) value).getFloorNumber());
                }
                return this;
            }
        });
        formPanel.add(floorCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // NOW load buildings (after floorCombo is created)
        loadBuildings();
        
        // Apartment number
        formPanel.add(createFieldLabel("Số Căn Hộ *"));
        apartmentNumberField = createTextField();
        formPanel.add(apartmentNumberField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Area
        formPanel.add(createFieldLabel("Diện Tích (m²) *"));
        areaField = createTextField();
        formPanel.add(areaField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Bedrooms spinner
        formPanel.add(createFieldLabel("Số Phòng Ngủ *"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 0, 10, 1);
        bedroomsSpinner = new JSpinner(spinnerModel);
        bedroomsSpinner.setFont(UIConstants.FONT_REGULAR);
        bedroomsSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        bedroomsSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JComponent editor = bedroomsSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setHorizontalAlignment(JTextField.LEFT);
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(new EmptyBorder(10, 12, 10, 12));
        }
        bedroomsSpinner.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        formPanel.add(bedroomsSpinner);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Status combo
        formPanel.add(createFieldLabel("Trạng Thái *"));
        statusCombo = new JComboBox<>(new String[]{"Available", "Occupied", "Maintenance"});
        statusCombo.setFont(UIConstants.FONT_REGULAR);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(statusCombo);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        ModernButton cancelButton = new ModernButton("Hủy", UIConstants.TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        ModernButton saveButton = new ModernButton(
            apartment == null ? "Thêm" : "Lưu", 
            UIConstants.SUCCESS_COLOR
        );
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> saveApartment());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        return buttonPanel;
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(UIConstants.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        
        // Focus border effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
                    new EmptyBorder(9, 11, 9, 11)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
        
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
    
    private void loadApartmentData() {
        if (apartment != null) {
            // Load floor and building
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            if (floor != null) {
                Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                
                // Select building first
                if (building != null) {
                    for (int i = 0; i < buildingCombo.getItemCount(); i++) {
                        Building b = buildingCombo.getItemAt(i);
                        if (b.getId().equals(building.getId())) {
                            buildingCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                
                // Then select floor (after cascade loads floors)
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < floorCombo.getItemCount(); i++) {
                        Floor f = floorCombo.getItemAt(i);
                        if (f.getId().equals(floor.getId())) {
                            floorCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                });
            }
            
            // Load other fields
            apartmentNumberField.setText(apartment.getApartmentNumber());
            areaField.setText(apartment.getArea().toString());
            bedroomsSpinner.setValue(apartment.getBedrooms());
            statusCombo.setSelectedItem(apartment.getStatus());
        }
    }
    
    private void saveApartment() {
        if (!validateForm()) {
            return;
        }
        
        Floor selectedFloor = (Floor) floorCombo.getSelectedItem();
        String apartmentNumber = apartmentNumberField.getText().trim();
        BigDecimal area = new BigDecimal(areaField.getText().trim());
        int bedrooms = (Integer) bedroomsSpinner.getValue();
        String status = (String) statusCombo.getSelectedItem();
        
        if (apartment == null) {
            // Create new apartment
            apartment = new Apartment();
            apartment.setFloorId(selectedFloor.getId());
            apartment.setApartmentNumber(apartmentNumber);
            apartment.setArea(area);
            apartment.setBedrooms(bedrooms);
            apartment.setStatus(status);
        } else {
            // Update existing apartment
            apartment.setFloorId(selectedFloor.getId());
            apartment.setApartmentNumber(apartmentNumber);
            apartment.setArea(area);
            apartment.setBedrooms(bedrooms);
            apartment.setStatus(status);
        }
        
        confirmed = true;
        dispose();
    }
    
    private boolean validateForm() {
        // Check building
        if (buildingCombo.getSelectedItem() == null) {
            showError("Vui lòng chọn tòa nhà!");
            return false;
        }
        
        // Check floor
        if (floorCombo.getSelectedItem() == null) {
            showError("Vui lòng chọn tầng!");
            return false;
        }
        
        // Check apartment number
        if (apartmentNumberField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập số căn hộ!");
            apartmentNumberField.requestFocus();
            return false;
        }
        
        // Check area
        if (areaField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập diện tích!");
            areaField.requestFocus();
            return false;
        }
        
        try {
            BigDecimal area = new BigDecimal(areaField.getText().trim());
            if (area.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Diện tích phải lớn hơn 0!");
                areaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Diện tích không hợp lệ!");
            areaField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Cảnh Báo",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Check if user confirmed the dialog
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Get the apartment (new or updated)
     */
    public Apartment getApartment() {
        return apartment;
    }
}