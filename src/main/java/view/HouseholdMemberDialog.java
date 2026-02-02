package view;

import model.HouseholdMember;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Household Member Dialog - Add/Edit Household Member
 * Popup form for adding/editing household members
 */
public class HouseholdMemberDialog extends JDialog {
    
    private JTextField nameField;
    private JComboBox<String> relationshipCombo;
    private JComboBox<String> genderCombo;
    private JTextField dobField;
    private JTextField idCardField;
    private JTextField phoneField;
    private JCheckBox isActiveCheck;
    
    private HouseholdMember member;
    private boolean confirmed = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Constructor for Add mode
     */
    public HouseholdMemberDialog(JDialog parent, Long contractId) {
        this(parent, null, contractId);
    }
    
    /**
     * Constructor for Edit mode
     */
    public HouseholdMemberDialog(JDialog parent, HouseholdMember member, Long contractId) {
        super(parent, member == null ? "Thêm Thành Viên" : "Sửa Thành Viên", true);
        
        if (member == null) {
            this.member = new HouseholdMember();
            this.member.setContractId(contractId);
            this.member.setActive(true);
        } else {
            this.member = member;
        }
        
        initializeDialog();
        createContent();
        
        if (member != null) {
            loadMemberData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        setSize(500, 650);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
    }
    
    private void createContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createForm(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel(member.getId() == null ? "➕" : "✏️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(member.getId() == null ? "Thêm Thành Viên Mới" : "Sửa Thông Tin Thành Viên");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
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
        
        // Full name
        formPanel.add(createFieldLabel("Họ và Tên *"));
        nameField = createTextField();
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Relationship
        formPanel.add(createFieldLabel("Mối Quan Hệ *"));
        relationshipCombo = new JComboBox<>(new String[]{
            "Vợ", "Chồng", "Con", "Bố", "Mẹ", "Anh/Chị/Em", "Ông/Bà", "Khác"
        });
        relationshipCombo.setFont(UIConstants.FONT_REGULAR);
        relationshipCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        relationshipCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(relationshipCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Gender
        formPanel.add(createFieldLabel("Giới Tính *"));
        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        genderCombo.setFont(UIConstants.FONT_REGULAR);
        genderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        genderCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(genderCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Date of birth
        formPanel.add(createFieldLabel("Ngày Sinh (yyyy-MM-dd)"));
        dobField = createTextField();
        dobField.setToolTipText("Ví dụ: 1990-05-15");
        formPanel.add(dobField);
        formPanel.add(createHintLabel("Ví dụ: 1990-05-15 (Có thể để trống)"));
        formPanel.add(Box.createVerticalStrut(15));
        
        // ID Card
        formPanel.add(createFieldLabel("CMND/CCCD"));
        idCardField = createTextField();
        formPanel.add(idCardField);
        formPanel.add(createHintLabel("Có thể để trống"));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Phone
        formPanel.add(createFieldLabel("Số Điện Thoại"));
        phoneField = createTextField();
        formPanel.add(phoneField);
        formPanel.add(createHintLabel("Có thể để trống"));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Is Active checkbox
        isActiveCheck = new JCheckBox("Đang cư trú");
        isActiveCheck.setFont(UIConstants.FONT_REGULAR);
        isActiveCheck.setBackground(Color.WHITE);
        isActiveCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        isActiveCheck.setSelected(true);
        formPanel.add(isActiveCheck);
        
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
            member.getId() == null ? "Thêm" : "Lưu", 
            UIConstants.SUCCESS_COLOR
        );
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> saveMember());
        
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
    
    private void loadMemberData() {
        nameField.setText(member.getFullName());
        relationshipCombo.setSelectedItem(member.getRelationship());
        genderCombo.setSelectedItem(member.getGender());
        
        if (member.getDob() != null) {
            dobField.setText(dateFormat.format(member.getDob()));
        }
        
        idCardField.setText(member.getIdentityCard() != null ? member.getIdentityCard() : "");
        phoneField.setText(member.getPhone() != null ? member.getPhone() : "");
        isActiveCheck.setSelected(member.isActive());
    }
    
    private void saveMember() {
        if (!validateForm()) {
            return;
        }
        
        try {
            member.setFullName(nameField.getText().trim());
            member.setRelationship((String) relationshipCombo.getSelectedItem());
            member.setGender((String) genderCombo.getSelectedItem());
            
            // Date of birth (optional)
            String dobText = dobField.getText().trim();
            if (!dobText.isEmpty()) {
                member.setDob(dateFormat.parse(dobText));
            } else {
                member.setDob(null);
            }
            
            // Optional fields
            String idCard = idCardField.getText().trim();
            member.setIdentityCard(idCard.isEmpty() ? null : idCard);
            
            String phone = phoneField.getText().trim();
            member.setPhone(phone.isEmpty() ? null : phone);
            
            member.setActive(isActiveCheck.isSelected());
            
            confirmed = true;
            dispose();
            
        } catch (ParseException e) {
            showError("Định dạng ngày sinh không đúng! Vui lòng nhập theo định dạng yyyy-MM-dd");
        }
    }
    
    private boolean validateForm() {
        // Full name
        if (nameField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập họ và tên!");
            nameField.requestFocus();
            return false;
        }
        
        if (nameField.getText().trim().length() < 2) {
            showError("Họ và tên phải có ít nhất 2 ký tự!");
            nameField.requestFocus();
            return false;
        }
        
        // Validate date format if provided
        String dobText = dobField.getText().trim();
        if (!dobText.isEmpty()) {
            try {
                Date dob = dateFormat.parse(dobText);
                Date now = new Date();
                if (dob.after(now)) {
                    showError("Ngày sinh không thể là tương lai!");
                    dobField.requestFocus();
                    return false;
                }
            } catch (ParseException e) {
                showError("Định dạng ngày sinh không đúng! Vui lòng nhập theo định dạng yyyy-MM-dd (Ví dụ: 1990-05-15)");
                dobField.requestFocus();
                return false;
            }
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
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public HouseholdMember getMember() {
        return member;
    }
}