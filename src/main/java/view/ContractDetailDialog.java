package view;

import dao.*;
import model.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for viewing contract details
 */
public class ContractDetailDialog extends JDialog {
    
    private ContractDAO contractDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private ContractServiceDAO contractServiceDAO;
    private ContractHistoryDAO contractHistoryDAO;
    
    private Contract contract;
    private Apartment apartment;
    private Resident resident;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    public ContractDetailDialog(JFrame parent, Long contractId) {
        super(parent, "Chi Tiết Hợp Đồng", true);
        
        this.contractDAO = new ContractDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.residentDAO = new ResidentDAO();
        this.contractServiceDAO = new ContractServiceDAO();
        this.contractHistoryDAO = new ContractHistoryDAO();
        
        // Load contract data
        this.contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            JOptionPane.showMessageDialog(parent, 
                "Không tìm thấy hợp đồng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        this.apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
        this.resident = residentDAO.getResidentById(contract.getResidentId());
        
        initComponents();
        
        setSize(900, 750);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Header
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Status Badge
        mainPanel.add(createStatusBadge());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Contract Info Section
        mainPanel.add(createContractInfoSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Apartment Info Section
        mainPanel.add(createApartmentInfoSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Resident Info Section
        mainPanel.add(createResidentInfoSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Services Section
        mainPanel.add(createServicesSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Notes Section (if exists)
        if (contract.getNotes() != null && !contract.getNotes().trim().isEmpty()) {
            mainPanel.add(createNotesSection());
        }
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel("📋");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Hợp Đồng " + contract.getContractNumber());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        
        JLabel subtitleLabel = new JLabel(contract.getContractTypeDisplay());
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);
        
        leftPanel.add(iconLabel);
        leftPanel.add(textPanel);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createStatusBadge() {
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel badge = new JPanel();
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        
        String status = contract.getStatusDisplay();
        Color bgColor, fgColor;
        String icon;
        
        if ("Đang hiệu lực".equals(status)) {
            bgColor = new Color(232, 245, 233);
            fgColor = new Color(46, 125, 50);
            icon = "●";
        } else if ("Sắp hết hạn".equals(status)) {
            bgColor = new Color(255, 243, 224);
            fgColor = new Color(230, 126, 34);
            icon = "⚠";
        } else if ("Đã hết hạn".equals(status)) {
            bgColor = new Color(255, 235, 238);
            fgColor = new Color(211, 47, 47);
            icon = "✕";
        } else {
            bgColor = new Color(250, 250, 250);
            fgColor = new Color(158, 158, 158);
            icon = "○";
        }
        
        badge.setBackground(bgColor);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fgColor, 2, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        
        JLabel statusLabel = new JLabel(icon + " " + status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(fgColor);
        
        badge.add(statusLabel);
        badgePanel.add(badge);
        
        return badgePanel;
    }
    
    private JPanel createContractInfoSection() {
        JPanel section = createSection("📋 Thông Tin Hợp Đồng");
        section.setLayout(new GridLayout(4, 4, 15, 12));
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        // Row 1
        section.add(createInfoLabel("Số hợp đồng:"));
        section.add(createInfoValue(contract.getContractNumber()));
        section.add(createInfoLabel("Loại hợp đồng:"));
        section.add(createInfoValue(contract.getContractTypeDisplay()));
        
        // Row 2
        section.add(createInfoLabel("Ngày ký:"));
        section.add(createInfoValue(contract.getSignedDate() != null ? 
            dateFormat.format(contract.getSignedDate()) : "Chưa ký"));
        section.add(createInfoLabel("Ngày bắt đầu:"));
        section.add(createInfoValue(dateFormat.format(contract.getStartDate())));
        
        // Row 3
        section.add(createInfoLabel("Ngày kết thúc:"));
        section.add(createInfoValue(contract.getEndDate() != null ? 
            dateFormat.format(contract.getEndDate()) : "Vô thời hạn"));
        section.add(createInfoLabel("Tiền cọc:"));
        section.add(createInfoValue(currencyFormat.format(contract.getDepositAmount())));
        
        // Row 4
        section.add(createInfoLabel("Ngày tạo:"));
        section.add(createInfoValue(contract.getCreatedAt() != null ? 
            dateFormat.format(contract.getCreatedAt()) : "N/A"));
        section.add(createInfoLabel("Cập nhật lần cuối:"));
        section.add(createInfoValue(contract.getUpdatedAt() != null ? 
            dateFormat.format(contract.getUpdatedAt()) : "N/A"));
        
        return section;
    }
    
    private JPanel createApartmentInfoSection() {
        JPanel section = createSection("🏠 Thông Tin Căn Hộ");
        section.setLayout(new GridLayout(2, 4, 15, 12));
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        if (apartment != null) {
            // Row 1
            section.add(createInfoLabel("Căn hộ:"));
            section.add(createInfoValue(apartment.getRoomNumber()));
            section.add(createInfoLabel("Diện tích:"));
            section.add(createInfoValue(apartment.getArea() + " m²"));
            
            // Row 2
            section.add(createInfoLabel("Loại căn hộ:"));
            section.add(createInfoValue(apartment.getApartmentType()));
            section.add(createInfoLabel("Số phòng:"));
            section.add(createInfoValue(apartment.getBedroomCount() + " PN, " + 
                apartment.getBathroomCount() + " PT"));
        } else {
            section.add(createInfoValue("Không tìm thấy thông tin căn hộ"));
        }
        
        return section;
    }
    
    private JPanel createResidentInfoSection() {
        JPanel section = createSection("👤 Thông Tin Chủ Hộ");
        section.setLayout(new GridLayout(2, 4, 15, 12));
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        if (resident != null) {
            // Row 1
            section.add(createInfoLabel("Họ tên:"));
            section.add(createInfoValue(resident.getFullName()));
            section.add(createInfoLabel("Số điện thoại:"));
            section.add(createInfoValue(resident.getPhone()));
            
            // Row 2
            section.add(createInfoLabel("CCCD/CMND:"));
            section.add(createInfoValue(resident.getIdentityCard()));
            section.add(createInfoLabel("Email:"));
            section.add(createInfoValue(resident.getEmail() != null ? resident.getEmail() : "N/A"));
        } else {
            section.add(createInfoValue("Không tìm thấy thông tin cư dân"));
        }
        
        return section;
    }
    
    private JPanel createServicesSection() {
        JPanel section = createSection("🔧 Dịch Vụ Áp Dụng");
        section.setLayout(new BorderLayout(0, 10));
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        // Load services
        List<ContractService> services = contractServiceDAO.getServicesByContract(contract.getId());
        
        if (services.isEmpty()) {
            JLabel noDataLabel = new JLabel("Chưa có dịch vụ nào");
            noDataLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noDataLabel.setForeground(new Color(158, 158, 158));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            section.add(noDataLabel, BorderLayout.CENTER);
        } else {
            String[] columns = {"Dịch vụ", "Đơn giá", "Đơn vị", "Ngày áp dụng", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            for (ContractService cs : services) {
                Object[] row = {
                    cs.getServiceName(),
                    currencyFormat.format(cs.getUnitPrice()),
                    cs.getUnitTypeDisplay(),
                    dateFormat.format(cs.getAppliedDate()),
                    cs.getActiveStatusDisplay()
                };
                model.addRow(row);
            }
            
            JTable table = new JTable(model);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(35);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(new Color(250, 250, 250));
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(0, 150));
            section.add(scrollPane, BorderLayout.CENTER);
        }
        
        return section;
    }
    
    private JPanel createNotesSection() {
        JPanel section = createSection("📝 Ghi Chú");
        section.setLayout(new BorderLayout(0, 10));
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JTextArea txtNotes = new JTextArea(contract.getNotes());
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setEditable(false);
        txtNotes.setBackground(new Color(250, 250, 250));
        txtNotes.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        section.add(txtNotes, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        
        JButton btnEdit = createButton("✏️ Sửa", new Color(33, 150, 243));
        btnEdit.addActionListener(e -> editContract());
        
        JButton btnRenew = createButton("🔄 Gia hạn", new Color(76, 175, 80));
        btnRenew.addActionListener(e -> renewContract());
        btnRenew.setEnabled(contract.isActive());
        
        JButton btnTerminate = createButton("❌ Kết thúc", new Color(244, 67, 54));
        btnTerminate.addActionListener(e -> terminateContract());
        btnTerminate.setEnabled(contract.isActive());
        
        JButton btnClose = createButton("Đóng", new Color(158, 158, 158));
        btnClose.addActionListener(e -> dispose());
        
        panel.add(btnEdit);
        panel.add(btnRenew);
        panel.add(btnTerminate);
        panel.add(btnClose);
        
        return panel;
    }
    
    // ===== HELPER METHODS =====
    
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 15),
            new Color(66, 66, 66)
        ));
        return panel;
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(117, 117, 117));
        return label;
    }
    
    private JLabel createInfoValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(33, 33, 33));
        return label;
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ===== ACTION HANDLERS =====
    
    private void editContract() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ContractFormDialog dialog = new ContractFormDialog(parentFrame, contract);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // Reload contract data
            contract = contractDAO.getContractById(contract.getId());
            dispose();
            
            // Show success message
            JOptionPane.showMessageDialog(parentFrame,
                "Cập nhật hợp đồng thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void renewContract() {
        // Show renew dialog
        String input = JOptionPane.showInputDialog(this,
            "Nhập số tháng gia hạn:",
            "Gia Hạn Hợp Đồng",
            JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int months = Integer.parseInt(input.trim());
                
                if (months <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Số tháng phải lớn hơn 0!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Calculate new end date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                if (contract.getEndDate() != null) {
                    cal.setTime(contract.getEndDate());
                } else {
                    cal.setTime(new java.util.Date());
                }
                cal.add(java.util.Calendar.MONTH, months);
                java.util.Date newEndDate = cal.getTime();
                
                // Update contract
                if (contractDAO.renewContract(contract.getId(), newEndDate)) {
                    JOptionPane.showMessageDialog(this,
                        "Gia hạn hợp đồng thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Reload
                    contract = contractDAO.getContractById(contract.getId());
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Gia hạn hợp đồng thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Số tháng không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void terminateContract() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn kết thúc hợp đồng này?",
            "Xác Nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(this,
                "Nhập lý do kết thúc:",
                "Lý Do Kết Thúc",
                JOptionPane.QUESTION_MESSAGE);
            
            if (reason != null && !reason.trim().isEmpty()) {
                if (contractDAO.terminateContract(contract.getId(), reason)) {
                    JOptionPane.showMessageDialog(this,
                        "Kết thúc hợp đồng thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Kết thúc hợp đồng thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}