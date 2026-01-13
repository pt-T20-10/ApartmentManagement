package view;

import model.Building;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Building Dialog - Popup for Add/Edit Building
 * Modern design with validation
 */
public class BuildingDialog extends JDialog {
    
    private JTextField nameField;
    private JTextField addressField;
    private JTextField managerField;
    private JTextArea descriptionArea;
    
    private Building building;
    private boolean confirmed = false;
    
    /**
     * Constructor for Add mode
     */
    public BuildingDialog(JFrame parent) {
        this(parent, null);
    }
    
    /**
     * Constructor for Edit mode
     */
    public BuildingDialog(JFrame parent, Building building) {
        super(parent, building == null ? "Thêm Tòa Nhà" : "Sửa Tòa Nhà", true);
        this.building = building;
        
        initializeDialog();
        createContent();
        
        if (building != null) {
            loadBuildingData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        setSize(500, 550);
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
        
        JLabel iconLabel = new JLabel(building == null ? "➕" : "✏️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(building == null ? "Thêm Tòa Nhà Mới" : "Sửa Thông Tin Tòa Nhà");
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
            building == null ? "Thêm" : "Lưu", 
            UIConstants.SUCCESS_COLOR
        );
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> saveBuilding());
        
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
        
        // Add focus border effect
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
    
    private void loadBuildingData() {
        if (building != null) {
            nameField.setText(building.getName());
            addressField.setText(building.getAddress());
            managerField.setText(building.getManagerName());
            descriptionArea.setText(building.getDescription());
        }
    }
    
    private void saveBuilding() {
        if (!validateForm()) {
            return;
        }
        
        if (building == null) {
            // Create new building
            building = new Building(
                nameField.getText().trim(),
                addressField.getText().trim(),
                managerField.getText().trim(),
                descriptionArea.getText().trim()
            );
        } else {
            // Update existing building
            building.setName(nameField.getText().trim());
            building.setAddress(addressField.getText().trim());
            building.setManagerName(managerField.getText().trim());
            building.setDescription(descriptionArea.getText().trim());
        }
        
        confirmed = true;
        dispose();
    }
    
    private boolean validateForm() {
        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập tên tòa nhà!");
            nameField.requestFocus();
            return false;
        }
        
        // Address validation
        if (addressField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập địa chỉ!");
            addressField.requestFocus();
            return false;
        }
        
        // Name length validation
        if (nameField.getText().trim().length() < 2) {
            showError("Tên tòa nhà phải có ít nhất 2 ký tự!");
            nameField.requestFocus();
            return false;
        }
        
        // Address length validation
        if (addressField.getText().trim().length() < 5) {
            showError("Địa chỉ phải có ít nhất 5 ký tự!");
            addressField.requestFocus();
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
     * Get the building (new or updated)
     */
    public Building getBuilding() {
        return building;
    }
}