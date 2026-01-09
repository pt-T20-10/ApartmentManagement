package view;

import dao.ResidentDAO;
import model.Resident;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Resident Management Panel
 * Full CRUD operations for residents with date handling
 */
public class ResidentManagementPanel extends JPanel {
    
    private ResidentDAO residentDAO;
    private JTable residentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Form fields
    private JTextField fullNameField;
    private JTextField dobField;        // Format: yyyy-MM-dd
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField idCardField;
    
    private Resident selectedResident = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public ResidentManagementPanel() {
        this.residentDAO = new ResidentDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createFormPanel();
        
        loadResidents();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[R]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Cu Dan");
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
        searchButton.addActionListener(e -> searchResidents());
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadResidents();
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
        String[] columns = {"ID", "Ho Ten", "Ngay Sinh", "Dien Thoai", "Email", "CMND/CCCD"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        residentTable = new JTable(tableModel);
        residentTable.setFont(UIConstants.FONT_REGULAR);
        residentTable.setRowHeight(40);
        residentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        residentTable.setShowGrid(true);
        residentTable.setGridColor(UIConstants.BORDER_COLOR);
        residentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedResident();
            }
        });
        
        // Table header
        JTableHeader header = residentTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Column widths
        residentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        residentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        residentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        residentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        residentTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        residentTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(residentTable);
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
        JLabel formTitle = new JLabel("Thong Tin Cu Dan");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Full name field
        formPanel.add(createFieldLabel("Ho va Ten *"));
        fullNameField = createTextField();
        formPanel.add(fullNameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Date of birth field
        formPanel.add(createFieldLabel("Ngay Sinh (yyyy-MM-dd) *"));
        dobField = createTextField();
        dobField.setToolTipText("Vi du: 1990-05-15");
        formPanel.add(dobField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Phone field
        formPanel.add(createFieldLabel("Dien Thoai *"));
        phoneField = createTextField();
        formPanel.add(phoneField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Email field
        formPanel.add(createFieldLabel("Email"));
        emailField = createTextField();
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // ID card field
        formPanel.add(createFieldLabel("CMND/CCCD *"));
        idCardField = createTextField();
        formPanel.add(idCardField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton addButton = new ModernButton("[+] Them", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addResident());
        
        ModernButton updateButton = new ModernButton("[E] Sua", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateResident());
        
        ModernButton deleteButton = new ModernButton("[X] Xoa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteResident());
        
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
    
    private void loadResidents() {
        tableModel.setRowCount(0);
        List<Resident> residents = residentDAO.getAllResidents();
        
        for (Resident resident : residents) {
            String dobStr = (resident.getDob() != null) ? dateFormat.format(resident.getDob()) : "";
            
            Object[] row = {
                resident.getId(),
                resident.getFullName(),
                dobStr,
                resident.getPhone(),
                resident.getEmail(),
                resident.getIdCard()
            };
            tableModel.addRow(row);
        }
    }
    
    private void loadSelectedResident() {
        int selectedRow = residentTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedResident = residentDAO.getResidentById(id);
            
            if (selectedResident != null) {
                fullNameField.setText(selectedResident.getFullName());
                dobField.setText(selectedResident.getDob() != null ? dateFormat.format(selectedResident.getDob()) : "");
                phoneField.setText(selectedResident.getPhone());
                emailField.setText(selectedResident.getEmail());
                idCardField.setText(selectedResident.getIdCard());
            }
        }
    }
    
    private void addResident() {
        if (!validateForm()) {
            return;
        }
        
        try {
            Resident resident = new Resident();
            resident.setFullName(fullNameField.getText().trim());
            resident.setDob(dateFormat.parse(dobField.getText().trim()));
            resident.setPhone(phoneField.getText().trim());
            resident.setEmail(emailField.getText().trim());
            resident.setIdCard(idCardField.getText().trim());
            
            if (residentDAO.insertResident(resident)) {
                JOptionPane.showMessageDialog(this, 
                    "Them cu dan thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Them cu dan that bai!", 
                    "Loi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, 
                "Dinh dang ngay sinh khong dung! Vui long nhap theo dinh dang yyyy-MM-dd", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateResident() {
        if (selectedResident == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon cu dan can sua!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        try {
            selectedResident.setFullName(fullNameField.getText().trim());
            selectedResident.setDob(dateFormat.parse(dobField.getText().trim()));
            selectedResident.setPhone(phoneField.getText().trim());
            selectedResident.setEmail(emailField.getText().trim());
            selectedResident.setIdCard(idCardField.getText().trim());
            
            if (residentDAO.updateResident(selectedResident)) {
                JOptionPane.showMessageDialog(this, 
                    "Cap nhat cu dan thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cap nhat cu dan that bai!", 
                    "Loi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, 
                "Dinh dang ngay sinh khong dung!", 
                "Loi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteResident() {
        if (selectedResident == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon cu dan can xoa!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa cu dan '" + selectedResident.getFullName() + "'?",
            "Xac Nhan Xoa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (residentDAO.deleteResident(selectedResident.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Xoa cu dan thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xoa cu dan that bai!", 
                    "Loi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchResidents() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadResidents();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Resident> residents = residentDAO.searchResidentsByName(keyword);
        
        // Also search by phone and ID card
        List<Resident> allResidents = residentDAO.getAllResidents();
        for (Resident resident : allResidents) {
            if ((resident.getPhone() != null && resident.getPhone().contains(keyword)) ||
                (resident.getIdCard() != null && resident.getIdCard().contains(keyword))) {
                if (!residents.contains(resident)) {
                    residents.add(resident);
                }
            }
        }
        
        for (Resident resident : residents) {
            String dobStr = (resident.getDob() != null) ? dateFormat.format(resident.getDob()) : "";
            
            Object[] row = {
                resident.getId(),
                resident.getFullName(),
                dobStr,
                resident.getPhone(),
                resident.getEmail(),
                resident.getIdCard()
            };
            tableModel.addRow(row);
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Khong tim thay cu dan nao!", 
                "Thong Bao", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearForm() {
        fullNameField.setText("");
        dobField.setText("");
        phoneField.setText("");
        emailField.setText("");
        idCardField.setText("");
        selectedResident = null;
        residentTable.clearSelection();
    }
    
    private boolean validateForm() {
        // Full name
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap ho ten!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            fullNameField.requestFocus();
            return false;
        }
        
        // Date of birth
        if (dobField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap ngay sinh!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            dobField.requestFocus();
            return false;
        }
        
        // Validate date format
        try {
            dateFormat.parse(dobField.getText().trim());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, 
                "Dinh dang ngay sinh khong dung! Vui long nhap theo dinh dang yyyy-MM-dd (Vi du: 1990-05-15)", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            dobField.requestFocus();
            return false;
        }
        
        // Phone
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap so dien thoai!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, 
                "So dien thoai phai co 10-11 chu so!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        // ID card
        if (idCardField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui long nhap CMND/CCCD!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            idCardField.requestFocus();
            return false;
        }
        
        String idCard = idCardField.getText().trim();
        if (!idCard.matches("\\d{9}") && !idCard.matches("\\d{12}")) {
            JOptionPane.showMessageDialog(this, 
                "CMND/CCCD phai co 9 hoac 12 chu so!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            idCardField.requestFocus();
            return false;
        }
        
        // Email (optional but validate if provided)
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, 
                "Dinh dang email khong hop le!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
}