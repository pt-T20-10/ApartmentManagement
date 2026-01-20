package view;

import model.Service;
import util.UIConstants;
import util.ModernButton;
import util.MoneyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Service Dialog - Popup for Add/Edit Service
 * Fields: Name, Unit, Price (with VND formatting), Mandatory checkbox
 * NO Description field
 */
public class ServiceDialog extends JDialog {
    
    private JTextField nameField;
    private JTextField unitField;
    private JTextField unitPriceField;
    private JCheckBox isMandatoryCheckbox;
    
    private Service service;
    private boolean confirmed = false;
    
    /**
     * Constructor for Add mode
     */
    public ServiceDialog(JFrame parent) {
        this(parent, null);
    }
    
    /**
     * Constructor for Edit mode
     */
    public ServiceDialog(JFrame parent, Service service) {
        super(parent, service == null ? "Thêm Dịch Vụ" : "Sửa Dịch Vụ", true);
        this.service = service;
        
        initializeDialog();
        createContent();
        
        if (service != null) {
            loadServiceData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        setSize(550, 450); // Shorter without description
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
        
        JLabel iconLabel = new JLabel(service == null ? "➕" : "✏️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(service == null ? "Thêm Dịch Vụ Mới" : "Sửa Thông Tin Dịch Vụ");
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
        
        // Service name
        formPanel.add(createFieldLabel("Tên Dịch Vụ *"));
        nameField = createTextField();
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Unit
        formPanel.add(createFieldLabel("Đơn Vị (kWh, m³, người, tháng...) *"));
        unitField = createTextField();
        formPanel.add(unitField);
        JLabel unitHint = createHintLabel("Ví dụ: kWh, m³, người, tháng");
        formPanel.add(unitHint);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Unit price with VND formatting
        formPanel.add(createFieldLabel("Đơn Giá (VNĐ) *"));
        unitPriceField = MoneyFormatter.createMoneyField(42);
        unitPriceField.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitPriceField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        formPanel.add(unitPriceField);
        JLabel priceHint = createHintLabel("Nhập số tiền, tự động format: 50.000");
        formPanel.add(priceHint);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Mandatory checkbox
        isMandatoryCheckbox = new JCheckBox("Dịch vụ bắt buộc (tính tự động cho mọi căn hộ)");
        isMandatoryCheckbox.setFont(UIConstants.FONT_REGULAR);
        isMandatoryCheckbox.setBackground(Color.WHITE);
        isMandatoryCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(isMandatoryCheckbox);
        
        // NO DESCRIPTION FIELD - REMOVED!
        
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
            service == null ? "Thêm" : "Lưu", 
            UIConstants.SUCCESS_COLOR
        );
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> saveService());
        
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
    
    private JLabel createHintLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        label.setForeground(UIConstants.TEXT_SECONDARY);
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
    
    private void loadServiceData() {
        if (service != null) {
            nameField.setText(service.getName());
            unitField.setText(service.getUnit());
            
            // Load price with MoneyFormatter
            MoneyFormatter.setValue(unitPriceField, service.getUnitPrice().longValue());
            
            isMandatoryCheckbox.setSelected(service.isMandatory());
            
            // NO description field
        }
    }
    
    private void saveService() {
        if (!validateForm()) {
            return;
        }
        
        String name = nameField.getText().trim();
        String unit = unitField.getText().trim();
        
        // Get price from MoneyFormatter
        Long priceValue = MoneyFormatter.getValue(unitPriceField);
        BigDecimal unitPrice = BigDecimal.valueOf(priceValue != null ? priceValue : 0);
        
        boolean isMandatory = isMandatoryCheckbox.isSelected();
        
        if (service == null) {
            // Create new service
            service = new Service();
            service.setName(name);
            service.setUnit(unit);
            service.setUnitPrice(unitPrice);
            service.setMandatory(isMandatory);
            // NO description
        } else {
            // Update existing service
            service.setName(name);
            service.setUnit(unit);
            service.setUnitPrice(unitPrice);
            service.setMandatory(isMandatory);
            // NO description
        }
        
        confirmed = true;
        dispose();
    }
    
    private boolean validateForm() {
        // Service name
        if (nameField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập tên dịch vụ!");
            nameField.requestFocus();
            return false;
        }
        
        if (nameField.getText().trim().length() < 2) {
            showError("Tên dịch vụ phải có ít nhất 2 ký tự!");
            nameField.requestFocus();
            return false;
        }
        
        // Unit
        if (unitField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập đơn vị!");
            unitField.requestFocus();
            return false;
        }
        
        // Unit price
        Long priceValue = MoneyFormatter.getValue(unitPriceField);
        if (priceValue == null || priceValue == 0) {
            showError("Vui lòng nhập đơn giá!");
            unitPriceField.requestFocus();
            return false;
        }
        
        if (priceValue < 0) {
            showError("Đơn giá phải lớn hơn 0!");
            unitPriceField.requestFocus();
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
     * Get the service (new or updated)
     */
    public Service getService() {
        return service;
    }
}