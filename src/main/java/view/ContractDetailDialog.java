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
 * UPDATED: Added History tab + FIXED button text colors
 */
public class ContractDetailDialog extends JDialog {
    
    private ContractDAO contractDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private ContractServiceDAO contractServiceDAO;
    private ContractHistoryDAO contractHistoryDAO;
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    
    private Contract contract;
    private Apartment apartment;
    private Resident resident;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    // History panel
    private ContractHistoryPanel historyPanel;
    
    public ContractDetailDialog(JFrame parent, Long contractId) {
        super(parent, "Chi Tiết Hợp Đồng", true);
        
        this.contractDAO = new ContractDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.residentDAO = new ResidentDAO();
        this.contractServiceDAO = new ContractServiceDAO();
        this.contractHistoryDAO = new ContractHistoryDAO();
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
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
        
        setSize(950, 800);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Tabbed Pane (Main content)
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
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
        
        // Status Badge
        JPanel statusBadge = createStatusBadge();
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(statusBadge, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatusBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
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
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(fgColor);
        
        badge.add(statusLabel);
        
        return badge;
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Tab 1: Thông tin
        JPanel infoPanel = createInfoPanel();
        tabbedPane.addTab("📋 Thông tin", infoPanel);
        
        // Tab 2: Dịch vụ
        JPanel servicesPanel = createServicesPanel();
        tabbedPane.addTab("🔧 Dịch vụ", servicesPanel);
        
        // Tab 3: Lịch sử (NEW!)
        historyPanel = new ContractHistoryPanel(contract.getId());
        tabbedPane.addTab("📜 Lịch sử", historyPanel);
        
        return tabbedPane;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Contract Info Section
        panel.add(createContractInfoSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Apartment Info Section
        panel.add(createApartmentInfoSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Resident Info Section
        panel.add(createResidentInfoSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Notes Section (if exists)
        if (contract.getNotes() != null && !contract.getNotes().trim().isEmpty()) {
            panel.add(createNotesSection());
        }
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        
        return wrapperPanel;
    }
    
    private JPanel createServicesPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
    mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
    mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
    
    // Load services
    List<ContractService> services = contractServiceDAO.getServicesByContract(contract.getId());
    
    // Statistics Cards
    JPanel statsPanel = createServiceStatsPanel(services);
    mainPanel.add(statsPanel, BorderLayout.NORTH);
    
    if (services.isEmpty()) {
        // Modern empty state
        JPanel emptyPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dashed border
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
                    0, new float[]{9}, 0));
                g2d.drawRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 12, 12);
            }
        };
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(new EmptyBorder(60, 20, 60, 20));
        
        JPanel emptyContent = new JPanel();
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));
        emptyContent.setOpaque(false);
        
        JLabel emptyIcon = new JLabel("📦");
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyTitle = new JLabel("Chưa có dịch vụ");
        emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        emptyTitle.setForeground(new Color(117, 117, 117));
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyDesc = new JLabel("Hợp đồng này chưa đăng ký dịch vụ nào");
        emptyDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyDesc.setForeground(new Color(158, 158, 158));
        emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        emptyContent.add(emptyIcon);
        emptyContent.add(Box.createVerticalStrut(15));
        emptyContent.add(emptyTitle);
        emptyContent.add(Box.createVerticalStrut(8));
        emptyContent.add(emptyDesc);
        
        emptyPanel.add(emptyContent);
        mainPanel.add(emptyPanel, BorderLayout.CENTER);
    } else {
        // Service Table
        JPanel tablePanel = createServiceTable(services);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
    }
    
    return mainPanel;
}
    
    private JPanel createServiceStatsPanel(List<ContractService> services) {
    JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
    statsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
    statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
    
    int total = services.size();
    int active = (int) services.stream().filter(cs -> cs.isActive()).count();
    int inactive = total - active;
    
    // Card 1: Total
    JPanel card1 = createServiceStatCard("Tổng Dịch Vụ", String.valueOf(total), 
        new Color(99, 102, 241), "🔧");
    
    // Card 2: Active
    JPanel card2 = createServiceStatCard("Đang Hoạt Động", String.valueOf(active), 
        new Color(34, 197, 94), "✓");
    
    // Card 3: Inactive
    JPanel card3 = createServiceStatCard("Ngừng Hoạt Động", String.valueOf(inactive), 
        new Color(156, 163, 175), "○");
    
    statsPanel.add(card1);
    statsPanel.add(card2);
    statsPanel.add(card3);
    
    return statsPanel;
}
    
    private JPanel createServiceStatCard(String title, String value, Color color, String icon) {
    JPanel card = new JPanel(new BorderLayout(12, 0)) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 6));
            g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
            
            // Background
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            g2d.dispose();
            super.paintComponent(g);
        }
    };
    card.setOpaque(false);
    card.setBorder(new EmptyBorder(14, 16, 14, 16));
    
    JLabel iconLabel = new JLabel(icon);
    iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
    iconLabel.setForeground(color);
    
    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false);
    
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    titleLabel.setForeground(new Color(107, 114, 128));
    
    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    valueLabel.setForeground(new Color(33, 33, 33));
    
    textPanel.add(titleLabel);
    textPanel.add(Box.createVerticalStrut(3));
    textPanel.add(valueLabel);
    
    card.add(iconLabel, BorderLayout.WEST);
    card.add(textPanel, BorderLayout.CENTER);
    
    return card;
}

    
    private JPanel createServiceTable(List<ContractService> services) {
    JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBackground(Color.WHITE);
    tablePanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
    
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
    table.setRowHeight(50);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setSelectionBackground(new Color(243, 244, 246));
    table.setSelectionForeground(new Color(33, 33, 33));
    
    // Header
    table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    table.getTableHeader().setBackground(new Color(249, 250, 251));
    table.getTableHeader().setForeground(new Color(75, 85, 99));
    table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 45));
    table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
    
    // Center align header
    javax.swing.table.DefaultTableCellRenderer headerRenderer = 
        (javax.swing.table.DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
    headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    
    // Column renderers
    javax.swing.table.DefaultTableCellRenderer centerRenderer = 
        new javax.swing.table.DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    
    // Service name - left align
    table.getColumnModel().getColumn(0).setPreferredWidth(200);
    
    // Price - right align with highlight
    table.getColumnModel().getColumn(1).setPreferredWidth(120);
    table.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(new Color(22, 163, 74)); // Green for price
            return label;
        }
    });
    
    // Unit - center
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
    
    // Date - center
    table.getColumnModel().getColumn(3).setPreferredWidth(120);
    table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    
    // Status - badge renderer
    table.getColumnModel().getColumn(4).setPreferredWidth(150);
    table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            JLabel badge = new JLabel();
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(5, 12, 5, 12));
            badge.setOpaque(true);
            
            String status = (String) value;
            if ("Đang hoạt động".equals(status)) {
                badge.setText("✓ Hoạt động");
                badge.setBackground(new Color(220, 252, 231));
                badge.setForeground(new Color(22, 163, 74));
            } else {
                badge.setText("○ Ngừng");
                badge.setBackground(new Color(243, 244, 246));
                badge.setForeground(new Color(107, 114, 128));
            }
            
            panel.add(badge);
            return panel;
        }
    });
    
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(Color.WHITE);
    
    tablePanel.add(scrollPane, BorderLayout.CENTER);
    
    return tablePanel;
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
    section.setLayout(new GridLayout(3, 4, 15, 12));  // ← THAY ĐỔI: 3 rows thay vì 2
    section.setBorder(BorderFactory.createCompoundBorder(
        section.getBorder(),
        new EmptyBorder(15, 20, 15, 20)
    ));
    
    if (apartment != null) {
        // ✅ Lấy thông tin Tầng và Tòa nhà
        Floor floor = floorDAO.getFloorById(apartment.getFloorId());
        Building building = null;
        if (floor != null) {
            building = buildingDAO.getBuildingById(floor.getBuildingId());
        }
        
        // Row 1: Tòa nhà và Tầng
        section.add(createInfoLabel("Tòa nhà:"));
        section.add(createInfoValue(building != null ? building.getName() : "N/A"));
        section.add(createInfoLabel("Tầng:"));
        section.add(createInfoValue(floor != null ? 
            (floor.getName() != null && !floor.getName().trim().isEmpty() ? 
                floor.getName() : "Tầng " + floor.getFloorNumber()) 
            : "N/A"));
        
        // Row 2: Căn hộ và Diện tích
        section.add(createInfoLabel("Căn hộ:"));
        section.add(createInfoValue(apartment.getRoomNumber()));
        section.add(createInfoLabel("Diện tích:"));
        section.add(createInfoValue(apartment.getArea() + " m²"));
        
        // Row 3: Loại căn hộ và Số phòng
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
        
        // ✅ NÚT GIA HẠN - FIX CỨNG MÀU CHỮ TRẮNG
        JButton btnRenew = createButton("🔄 Gia hạn", new Color(76, 175, 80));
        btnRenew.setForeground(Color.WHITE);  // ← FIX CỨNG
        btnRenew.addActionListener(e -> renewContract());
        btnRenew.setEnabled(contract.isActive());
        
        // ✅ NÚT KẾT THÚC - FIX CỨNG MÀU CHỮ TRẮNG
        JButton btnTerminate = createButton("❌ Kết thúc", new Color(244, 67, 54));
        btnTerminate.setForeground(Color.WHITE);  // ← FIX CỨNG
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
    
    // ✅ FIXED: Button with white text
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);              // ← CRITICAL: Makes background visible
        btn.setContentAreaFilled(true);   // ← CRITICAL: Fills the background
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
            
            // Refresh history panel
            if (historyPanel != null) {
                historyPanel.refresh();
            }
            
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
                    
                    // Refresh history
                    if (historyPanel != null) {
                        historyPanel.refresh();
                    }
                    
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
                    
                    // Refresh history
                    if (historyPanel != null) {
                        historyPanel.refresh();
                    }
                    
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