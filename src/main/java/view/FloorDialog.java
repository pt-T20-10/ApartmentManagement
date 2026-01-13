package view;

import model.Floor;
import model.Building;
import dao.BuildingDAO;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Floor Dialog - Popup for Add/Edit Floor
 * Modern design with building selection
 */
public class FloorDialog extends JDialog {
    
    private BuildingDAO buildingDAO;
    private JComboBox<Building> buildingCombo;
    private JSpinner floorNumberSpinner;
    private JTextArea descriptionArea;
    
    private Floor floor;
    private boolean confirmed = false;
    
    /**
     * Constructor for Add mode
     */
    public FloorDialog(JFrame parent) {
        this(parent, null);
    }
    
    /**
     * Constructor for Edit mode
     */
    public FloorDialog(JFrame parent, Floor floor) {
        super(parent, floor == null ? "Thêm Tầng" : "Sửa Tầng", true);
        this.floor = floor;
        this.buildingDAO = new BuildingDAO();
        
        initializeDialog();
        createContent();
        
        if (floor != null) {
            loadFloorData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        setSize(500, 500);
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
        
        JLabel iconLabel = new JLabel(floor == null ? "➕" : "✏️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(floor == null ? "Thêm Tầng Mới" : "Sửa Thông Tin Tầng");
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
        loadBuildings();
        formPanel.add(buildingCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Floor number spinner
        formPanel.add(createFieldLabel("Số Tầng *"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
        floorNumberSpinner = new JSpinner(spinnerModel);
        floorNumberSpinner.setFont(UIConstants.FONT_REGULAR);
        floorNumberSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        floorNumberSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Style spinner
        JComponent editor = floorNumberSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setHorizontalAlignment(JTextField.LEFT);
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(new EmptyBorder(10, 12, 10, 12));
        }
        floorNumberSpinner.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        formPanel.add(floorNumberSpinner);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Description field
        formPanel.add(createFieldLabel("Mô Tả"));
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(UIConstants.FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        formPanel.add(scrollPane);
        
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
            floor == null ? "Thêm" : "Lưu", 
            UIConstants.SUCCESS_COLOR
        );
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> saveFloor());
        
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
    
    private void loadBuildings() {
        DefaultComboBoxModel<Building> model = new DefaultComboBoxModel<>();
        List<Building> buildings = buildingDAO.getAllBuildings();
        
        for (Building building : buildings) {
            model.addElement(building);
        }
        
        buildingCombo.setModel(model);
    }
    
    private void loadFloorData() {
        if (floor != null) {
            // Select building
            Building building = buildingDAO.getBuildingById(floor.getBuildingId());
            if (building != null) {
                for (int i = 0; i < buildingCombo.getItemCount(); i++) {
                    Building b = buildingCombo.getItemAt(i);
                    if (b.getId().equals(building.getId())) {
                        buildingCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            floorNumberSpinner.setValue(floor.getFloorNumber());
            descriptionArea.setText(floor.getDescription());
        }
    }
    
    private void saveFloor() {
        if (!validateForm()) {
            return;
        }
        
        Building selectedBuilding = (Building) buildingCombo.getSelectedItem();
        int floorNumber = (Integer) floorNumberSpinner.getValue();
        String description = descriptionArea.getText().trim();
        
        if (floor == null) {
            // Create new floor
            floor = new Floor();
            floor.setBuildingId(selectedBuilding.getId());
            floor.setFloorNumber(floorNumber);
            floor.setDescription(description);
        } else {
            // Update existing floor
            floor.setBuildingId(selectedBuilding.getId());
            floor.setFloorNumber(floorNumber);
            floor.setDescription(description);
        }
        
        confirmed = true;
        dispose();
    }
    
    private boolean validateForm() {
        // Check building selection
        if (buildingCombo.getSelectedItem() == null) {
            showError("Vui lòng chọn tòa nhà!");
            return false;
        }
        
        // Floor number validation (already handled by spinner, but double check)
        int floorNumber = (Integer) floorNumberSpinner.getValue();
        if (floorNumber < 0) {
            showError("Số tầng phải lớn hơn hoặc bằng 0!");
            floorNumberSpinner.requestFocus();
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
     * Get the floor (new or updated)
     */
    public Floor getFloor() {
        return floor;
    }
}