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
import java.util.List;

/**
 * Resident Management Panel
 * Full CRUD operations with popup dialog
 */
public class ResidentManagementPanel extends JPanel {
    
    private ResidentDAO residentDAO;
    private JTable residentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public ResidentManagementPanel() {
        this.residentDAO = new ResidentDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadResidents();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Cư Dân");
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
        searchField.addActionListener(e -> searchResidents());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchResidents());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
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
        String[] columns = {"ID", "Họ Tên", "Ngày Sinh", "Điện Thoại", "Email", "CMND/CCCD"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        residentTable = new JTable(tableModel);
        residentTable.setFont(UIConstants.FONT_REGULAR);
        residentTable.setRowHeight(45);
        residentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        residentTable.setShowGrid(true);
        residentTable.setGridColor(UIConstants.BORDER_COLOR);
        
        // Double-click to edit
        residentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editResident();
                }
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
        residentTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        residentTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        residentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        residentTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        residentTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        
        JScrollPane scrollPane = new JScrollPane(residentTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Cư Dân", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addResident());
        
        ModernButton editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editResident());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteResident());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
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
                resident.getEmail() != null ? resident.getEmail() : "",
                resident.getIdCard()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addResident() {
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog
        ResidentDialog dialog = new ResidentDialog(parentFrame);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Resident resident = dialog.getResident();
            
            if (residentDAO.insertResident(resident)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm cư dân thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm cư dân thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editResident() {
        int selectedRow = residentTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn cư dân cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Resident resident = residentDAO.getResidentById(id);
        
        if (resident == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy cư dân!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog with existing resident
        ResidentDialog dialog = new ResidentDialog(parentFrame, resident);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Resident updatedResident = dialog.getResident();
            
            if (residentDAO.updateResident(updatedResident)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật cư dân thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật cư dân thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteResident() {
        int selectedRow = residentTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn cư dân cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String fullName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa cư dân '" + fullName + "'?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (residentDAO.deleteResident(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa cư dân thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa cư dân thất bại!", 
                    "Lỗi", 
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
                resident.getEmail() != null ? resident.getEmail() : "",
                resident.getIdCard()
            };
            tableModel.addRow(row);
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy cư dân nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}