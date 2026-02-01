package view;

import dao.*;
import model.*;
import util.UIConstants;
import util.ModernButton;
import util.PermissionManager; // Import PermissionManager

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;

/**
 * Invoice Management Panel - Cải tiến Tích hợp đầy đủ với InvoiceFormDialog và
 * InvoiceDetailDialog
 */
public class InvoiceManagementPanel extends JPanel {

    // DAOs
    private InvoiceDAO invoiceDAO;
    private ApartmentDAO apartmentDAO;
    private ContractDAO contractDAO;
    private ResidentDAO residentDAO;
    private FloorDAO floorDAO; // Thêm FloorDAO để check building ID từ apartment
    private PermissionManager permissionManager; // Khai báo

    private JPanel mainContainer;

    // Tables
    private JTable invoiceTable;
    private DefaultTableModel tableModel;

    // Filters
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JComboBox<String> statusCombo;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;
    // Selected data
    private Invoice selectedInvoice = null;

    // Formatters
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Buttons
    private ModernButton btnCreate;
    private ModernButton btnView;
    private ModernButton btnPay;
    private ModernButton btnCancel;

    // Statistics labels
    private JLabel lblTotalInvoices;
    private JLabel lblUnpaidInvoices;
    private JLabel lblTotalRevenue;

    public InvoiceManagementPanel() {
        initializeDAOs();
        this.permissionManager = PermissionManager.getInstance();

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);

        // Create main container with padding
        mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Top section: Header + Statistics
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(UIConstants.BACKGROUND_COLOR);

        // Add header
        JPanel headerPanel = createHeader();
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(headerPanel);
        topSection.add(Box.createVerticalStrut(20));

        // Add statistics
        JPanel statsPanel = createStatisticsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(statsPanel);

        mainContainer.add(topSection, BorderLayout.NORTH);

        // Add main content
        createMainPanel();

        add(mainContainer, BorderLayout.CENTER);

        loadInvoices();
        updateStatistics();
    }

    private void initializeDAOs() {
        this.invoiceDAO = new InvoiceDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.contractDAO = new ContractDAO();
        this.residentDAO = new ResidentDAO();
        this.floorDAO = new FloorDAO();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel iconLabel = new JLabel("💰");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel("Quản Lý Hóa Đơn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);

        JPanel filterPanel = createFilterPanel();

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setToolTipText("Tìm theo căn hộ, cư dân...");
        txtSearch.addActionListener(e -> filterInvoices());
        panel.add(new JLabel("🔍"));
        panel.add(txtSearch);

        panel.add(Box.createHorizontalStrut(10));

        panel.add(new JLabel("Tháng:"));
        monthCombo = new JComboBox<>();
        monthCombo.addItem(0); 
        for (int i = 1; i <= 12; i++) {
            monthCombo.addItem(i);
        }
        Calendar cal = Calendar.getInstance();
        monthCombo.setSelectedItem(cal.get(Calendar.MONTH) + 1);
        monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthCombo.addActionListener(e -> filterInvoices());
        panel.add(monthCombo);

        panel.add(new JLabel("Năm:"));
        yearCombo = new JComboBox<>();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = currentYear - 2; i <= currentYear + 1; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.setSelectedItem(currentYear);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearCombo.addActionListener(e -> filterInvoices());
        panel.add(yearCombo);

        panel.add(new JLabel("Trạng thái:"));
        statusCombo = new JComboBox<>(new String[]{
            "Tất cả", "Chưa thanh toán", "Đã thanh toán", "Đã hủy"
        });

        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.addActionListener(e -> filterInvoices());
        panel.add(statusCombo);

        panel.add(Box.createHorizontalStrut(10));

        ModernButton refreshButton = new ModernButton("🔄 Làm mới", UIConstants.INFO_COLOR);
        refreshButton.addActionListener(e -> {
            loadInvoices();
            updateStatistics();
        });
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        StatCardResult totalCard = createStatCard("📊 Tổng hóa đơn", "0", new Color(33, 150, 243));
        lblTotalInvoices = totalCard.valueLabel;
        statsPanel.add(totalCard.panel);

        StatCardResult unpaidCard = createStatCard("⏳ Chưa thanh toán", "0", new Color(255, 152, 0));
        lblUnpaidInvoices = unpaidCard.valueLabel;
        statsPanel.add(unpaidCard.panel);

        StatCardResult revenueCard = createStatCard("💵 Tổng doanh thu", "0 VNĐ", new Color(46, 125, 50));
        lblTotalRevenue = revenueCard.valueLabel;
        statsPanel.add(revenueCard.panel);

        return statsPanel;
    }

    private static class StatCardResult {
        JPanel panel;
        JLabel valueLabel;

        StatCardResult(JPanel panel, JLabel valueLabel) {
            this.panel = panel;
            this.valueLabel = valueLabel;
        }
    }

    private StatCardResult createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(accentColor);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(5));
        content.add(lblValue);

        card.add(content, BorderLayout.CENTER);

        return new StatCardResult(card, lblValue);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel tablePanel = createTablePanel();
        JPanel actionPanel = createActionPanel();

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.EAST);

        mainContainer.add(mainPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel tableTitle = new JLabel("📋 Danh Sách Hóa Đơn");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(UIConstants.TEXT_PRIMARY);
        tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Số HĐ", "Căn hộ", "Cư dân",
            "Tháng/Năm", "Tổng tiền", "Trạng thái", "Ngày TT"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Long.class;
                if (columnIndex == 5) return BigDecimal.class;
                return String.class;
            }
        };

        invoiceTable = new JTable(tableModel);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invoiceTable.setRowHeight(40);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setSelectionBackground(new Color(232, 245, 255));
        invoiceTable.setSelectionForeground(Color.BLACK);
        invoiceTable.setShowGrid(true);
        invoiceTable.setGridColor(new Color(240, 240, 240));

        sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(
                new RowSorter.SortKey(0, SortOrder.DESCENDING)
        ));
        invoiceTable.setRowSorter(sorter);

        TableColumnModel colModel = invoiceTable.getColumnModel();
        TableColumn idCol = colModel.getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        colModel.getColumn(1).setPreferredWidth(100);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(3).setPreferredWidth(150);
        colModel.getColumn(4).setPreferredWidth(100);
        colModel.getColumn(5).setPreferredWidth(120);
        colModel.getColumn(6).setPreferredWidth(120);
        colModel.getColumn(7).setPreferredWidth(100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        colModel.getColumn(4).setCellRenderer(centerRenderer);
        colModel.getColumn(7).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value instanceof BigDecimal) {
                    setText(moneyFormat.format(value) + " VNĐ");
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                }
                return this;
            }
        };
        colModel.getColumn(5).setCellRenderer(moneyRenderer);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value != null) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (!isSelected) {
                        if ("Đã thanh toán".equals(value)) {
                            setBackground(new Color(232, 245, 233));
                            setForeground(new Color(46, 125, 50));
                        } else {
                            setBackground(new Color(255, 243, 224));
                            setForeground(new Color(230, 126, 34));
                        }
                    }
                }
                return this;
            }
        };
        colModel.getColumn(6).setCellRenderer(statusRenderer);

        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onInvoiceSelected();
            }
        });

        invoiceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && invoiceTable.getSelectedRow() != -1) {
                    openInvoiceDetailFromTable();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void openInvoiceDetailFromTable() {
        int viewRow = invoiceTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = invoiceTable.convertRowIndexToModel(viewRow);
        Long invoiceId = (Long) tableModel.getValueAt(modelRow, 0);

        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        InvoiceDetailDialog dialog = new InvoiceDetailDialog(parent, invoiceId);
        dialog.setVisible(true);

        loadInvoices();
        updateStatistics();
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel actionTitle = new JLabel("⚙️ Thao Tác");
        actionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        actionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(actionTitle);
        panel.add(Box.createVerticalStrut(20));

        // --- CHECK QUYỀN NÚT BẤM ---
        
        // 1. Tạo Hóa Đơn (canAdd)
        btnCreate = new ModernButton("➕ Tạo Hóa Đơn", new Color(33, 150, 243));
        btnCreate.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreate.setMaximumSize(new Dimension(190, 45));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createInvoice());
        btnCreate.setVisible(permissionManager.canAdd(PermissionManager.MODULE_INVOICES)); // Check quyền
        panel.add(btnCreate);
        panel.add(Box.createVerticalStrut(10));

        // 2. Xem Chi Tiết
        btnView = new ModernButton("👁️ Xem Chi Tiết", new Color(76, 175, 80));
        btnView.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnView.setMaximumSize(new Dimension(190, 45));
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnView.setEnabled(false);
        btnView.addActionListener(e -> viewInvoiceDetail());
        panel.add(btnView);
        panel.add(Box.createVerticalStrut(10));

        // 3. Thanh Toán (canEdit)
        btnPay = new ModernButton("💰 Thanh Toán", new Color(46, 125, 50));
        btnPay.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPay.setMaximumSize(new Dimension(190, 45));
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.setEnabled(false);
        btnPay.addActionListener(e -> markAsPaid());
        btnPay.setVisible(permissionManager.canEdit(PermissionManager.MODULE_INVOICES)); // Check quyền
        panel.add(btnPay);
        panel.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(190, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(20));

        // 4. Hủy (canDelete)
        btnCancel = new ModernButton("Hủy HĐ", new Color(244, 67, 54));
        btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(190, 45));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(e -> cancelInvoice());
        btnCancel.setVisible(permissionManager.canDelete(PermissionManager.MODULE_INVOICES)); // Check quyền
        panel.add(btnCancel);

        panel.add(Box.createVerticalGlue());

        JLabel infoLabel = new JLabel("<html><center>💡 Chọn hóa đơn<br>để thực hiện thao tác</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);

        return panel;
    }

    // ===== DATA LOADING (FILTERED) =====
    private void loadInvoices() {
        tableModel.setRowCount(0);
        List<Invoice> allInvoices = invoiceDAO.getAllInvoices();
        
        // Lấy Building Filter
        Long filterId = permissionManager.getBuildingFilter();

        for (Invoice invoice : allInvoices) {
            // Lấy thông tin HĐ để biết tòa nhà
            Contract contract = contractDAO.getContractById(invoice.getContractId());
            if (contract == null) continue;
            
            // Nếu có filter (Manager/Staff), kiểm tra căn hộ thuộc tòa nhà
            if (filterId != null) {
                Apartment apt = apartmentDAO.getApartmentById(contract.getApartmentId());
                if (apt == null) continue;
                
                // Lấy Floor để check Building
                Floor floor = floorDAO.getFloorById(apt.getFloorId());
                if (floor == null || !floor.getBuildingId().equals(filterId)) {
                    continue; // Bỏ qua nếu không đúng tòa nhà
                }
            }

            addInvoiceToTable(invoice);
        }
    }

    private void addInvoiceToTable(Invoice invoice) {
        Contract contract = contractDAO.getContractById(invoice.getContractId());

        String contractNumber = "N/A";
        String apartmentInfo = "N/A";
        String residentInfo = "N/A";

        if (contract != null) {
            contractNumber = contract.getContractNumber() != null ? contract.getContractNumber() : "N/A";

            Apartment apt = apartmentDAO.getApartmentById(contract.getApartmentId());
            if (apt != null) apartmentInfo = apt.getRoomNumber();

            Resident res = residentDAO.getResidentById(contract.getResidentId());
            if (res != null) residentInfo = res.getFullName();
        }

        String monthYear = String.format("Tháng %d/%d", invoice.getMonth(), invoice.getYear());

        String statusDisplay;
        switch (invoice.getStatus()) {
            case "PAID": statusDisplay = "Đã thanh toán"; break;
            case "CANCELED": statusDisplay = "Đã hủy"; break;
            default: statusDisplay = "Chưa thanh toán";
        }

        String paymentDate = "";
        if (invoice.getPaymentDate() != null) {
            paymentDate = dateFormat.format(invoice.getPaymentDate());
        }

        Object[] row = {
            invoice.getId(),
            contractNumber,
            apartmentInfo,
            residentInfo,
            monthYear,
            invoice.getTotalAmount(),
            statusDisplay,
            paymentDate
        };

        tableModel.addRow(row);
    }

    private void filterInvoices() {
        // Logic filter ở UI (trên list đã load)
        // Reset list bằng cách reload lại từ DB (đã qua filter building) rồi áp dụng filter UI
        // Hoặc tối ưu hơn: Lưu cached list. Ở đây gọi loadInvoices() sẽ reset.
        // Tuy nhiên loadInvoices lại load ALL từ DB.
        
        // Cách tốt nhất: Load danh sách (đã filter building) vào biến tạm, rồi filter UI.
        // Để đơn giản và nhanh, ta sửa lại filterInvoices để filter trên rowSorter hoặc reload list.
        
        // SỬA: Load list gốc (filtered building) trước
        tableModel.setRowCount(0);
        
        Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
        Integer selectedYear = (Integer) yearCombo.getSelectedItem();
        String selectedStatus = (String) statusCombo.getSelectedItem();
        String searchText = txtSearch.getText().trim().toLowerCase();
        
        // 1. Load data từ DB (có filter building)
        List<Invoice> invoices = new ArrayList<>();
        List<Invoice> allInvoices = invoiceDAO.getAllInvoices();
        Long filterId = permissionManager.getBuildingFilter();
        
        for(Invoice inv : allInvoices) {
             Contract contract = contractDAO.getContractById(inv.getContractId());
             if (contract == null) continue;
             if (filterId != null) {
                 Apartment apt = apartmentDAO.getApartmentById(contract.getApartmentId());
                 if(apt == null) continue;
                 Floor floor = floorDAO.getFloorById(apt.getFloorId());
                 if(floor == null || !floor.getBuildingId().equals(filterId)) continue;
             }
             invoices.add(inv);
        }

        // 2. Filter UI
        // Month/Year
        if (selectedMonth != 0) {
            invoices.removeIf(inv -> inv.getMonth() != selectedMonth);
        }
        invoices.removeIf(inv -> inv.getYear() != selectedYear);

        // Status
        if (!"Tất cả".equals(selectedStatus)) {
            String statusFilter = "UNPAID";
            if ("Đã thanh toán".equals(selectedStatus)) statusFilter = "PAID";
            else if ("Đã hủy".equals(selectedStatus)) statusFilter = "CANCELED";
            
            final String finalStatus = statusFilter;
            invoices.removeIf(inv -> !finalStatus.equals(inv.getStatus()));
        }

        // Search text
        if (!searchText.isEmpty()) {
            invoices.removeIf(inv -> {
                Contract contract = contractDAO.getContractById(inv.getContractId());
                if (contract == null) return true;

                Apartment apt = apartmentDAO.getApartmentById(contract.getApartmentId());
                Resident res = residentDAO.getResidentById(contract.getResidentId());

                String aptNumber = apt != null ? apt.getRoomNumber().toLowerCase() : "";
                String resName = res != null ? res.getFullName().toLowerCase() : "";
                String contractNum = contract.getContractNumber() != null ? contract.getContractNumber().toLowerCase() : "";

                return !aptNumber.contains(searchText)
                        && !resName.contains(searchText)
                        && !contractNum.contains(searchText);
            });
        }

        for (Invoice invoice : invoices) {
            addInvoiceToTable(invoice);
        }

        updateStatistics();
    }

    private void updateStatistics() {
        // Cần thống kê trên danh sách hiển thị (để đúng logic filter)
        // Hiện tại tableModel chứa danh sách hiển thị
        
        long totalCount = 0;
        long unpaidCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for(int i=0; i<tableModel.getRowCount(); i++) {
             // Cột status index 6, Total Amount index 5
             String status = (String) tableModel.getValueAt(i, 6);
             BigDecimal amount = (BigDecimal) tableModel.getValueAt(i, 5);
             
             if(!"Đã hủy".equals(status)) {
                 totalCount++;
             }
             if("Chưa thanh toán".equals(status)) {
                 unpaidCount++;
             }
             if("Đã thanh toán".equals(status)) {
                 totalRevenue = totalRevenue.add(amount);
             }
        }

        lblTotalInvoices.setText(String.valueOf(totalCount));
        lblUnpaidInvoices.setText(String.valueOf(unpaidCount));
        lblTotalRevenue.setText(moneyFormat.format(totalRevenue) + " VNĐ");
    }

    private void onInvoiceSelected() {
        int viewRow = invoiceTable.getSelectedRow();
        if (viewRow < 0) {
            selectedInvoice = null;
            btnView.setEnabled(false);
            btnPay.setEnabled(false);
            btnCancel.setEnabled(false);
            return;
        }

        int modelRow = invoiceTable.convertRowIndexToModel(viewRow);
        Long invoiceId = (Long) tableModel.getValueAt(modelRow, 0);

        selectedInvoice = invoiceDAO.getInvoiceById(invoiceId);

        btnView.setEnabled(true);
        String status = selectedInvoice.getStatus();
        boolean isUnpaid = "UNPAID".equals(status);

        // Check quyền enable/disable
        btnPay.setEnabled(isUnpaid && permissionManager.canEdit(PermissionManager.MODULE_INVOICES));
        btnCancel.setEnabled(isUnpaid && permissionManager.canDelete(PermissionManager.MODULE_INVOICES));
    }

    private void createInvoice() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        InvoiceFormDialog dialog = new InvoiceFormDialog(parentFrame);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            loadInvoices(); // Sẽ tự động filter theo building
            updateStatistics();
            JOptionPane.showMessageDialog(this, "Tạo hóa đơn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewInvoiceDetail() {
        if (selectedInvoice == null) return;
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        InvoiceDetailDialog dialog = new InvoiceDetailDialog(parentFrame, selectedInvoice.getId());
        dialog.setVisible(true);
        loadInvoices();
        updateStatistics();
    }

    private void markAsPaid() {
        if (selectedInvoice == null) return;
        
        // Double check quyền
        if (!permissionManager.canEdit(PermissionManager.MODULE_INVOICES)) {
            permissionManager.showAccessDeniedMessage(this, "thanh toán");
            return;
        }

        if ("PAID".equals(selectedInvoice.getStatus())) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận đã thanh toán hóa đơn này?\n\nSố tiền: %s VNĐ",
                        moneyFormat.format(selectedInvoice.getTotalAmount())),
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedInvoice.setStatus("PAID");
            selectedInvoice.setPaymentDate(new Date());

            if (invoiceDAO.updateInvoice(selectedInvoice)) {
                JOptionPane.showMessageDialog(this, "Đánh dấu thanh toán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadInvoices();
                updateStatistics();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelInvoice() {
        if (selectedInvoice == null) return;

        // Double check quyền
        if (!permissionManager.canDelete(PermissionManager.MODULE_INVOICES)) {
            permissionManager.showAccessDeniedMessage(this, "hủy hóa đơn");
            return;
        }
        
        if ("PAID".equals(selectedInvoice.getStatus())) {
            JOptionPane.showMessageDialog(this, "Không thể hủy hóa đơn đã thanh toán!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn HỦY hóa đơn này?", "Xác nhận hủy", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedInvoice.setStatus("CANCELED");
            invoiceDAO.updateInvoice(selectedInvoice);
            loadInvoices();
            updateStatistics();
        }
    }
}