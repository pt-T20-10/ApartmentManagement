package view;

import dao.*;
import model.*;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Invoice Management Panel
 * Master-Detail pattern for invoices and invoice details
 */
public class InvoiceManagementPanel extends JPanel {
    
    private InvoiceDAO invoiceDAO;
    private ApartmentDAO apartmentDAO;
    private ServiceDAO serviceDAO;
    private ContractDAO contractDAO;
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JTable detailTable;
    private DefaultTableModel detailTableModel;
    
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JComboBox<String> statusCombo;
    
    private Invoice selectedInvoice = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public InvoiceManagementPanel() {
        this.invoiceDAO = new InvoiceDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.serviceDAO = new ServiceDAO();
        this.contractDAO = new ContractDAO();
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createMainPanel();
        
        loadInvoices();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[I]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Hoa Don");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        filterPanel.add(new JLabel("Thang:"));
        monthCombo = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthCombo.addItem(i);
        }
        Calendar cal = Calendar.getInstance();
        monthCombo.setSelectedItem(cal.get(Calendar.MONTH) + 1);
        filterPanel.add(monthCombo);
        
        filterPanel.add(new JLabel("Nam:"));
        yearCombo = new JComboBox<>();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.setSelectedItem(currentYear);
        filterPanel.add(yearCombo);
        
        filterPanel.add(new JLabel("Trang thai:"));
        statusCombo = new JComboBox<>(new String[]{"All", "Pending", "Paid", "Overdue"});
        filterPanel.add(statusCombo);
        
        ModernButton filterButton = new ModernButton("[F] Loc", UIConstants.INFO_COLOR);
        filterButton.addActionListener(e -> filterInvoices());
        filterPanel.add(filterButton);
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> loadInvoices());
        filterPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Top: Invoice table
        JPanel invoicePanel = createInvoiceTablePanel();
        
        // Bottom: Detail table + Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel detailPanel = createDetailTablePanel();
        JPanel actionPanel = createActionPanel();
        
        bottomPanel.add(detailPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        
        // Split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, invoicePanel, bottomPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(300);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createInvoiceTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Danh Sach Hoa Don"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"ID", "Can Ho", "Thang/Nam", "Tong Tien", "Trang Thai", "Ngay TT"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoiceTable = new JTable(tableModel);
        invoiceTable.setFont(UIConstants.FONT_REGULAR);
        invoiceTable.setRowHeight(35);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedInvoice();
            }
        });
        
        JTableHeader header = invoiceTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Chi Tiet Hoa Don"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"Dich Vu", "So Luong", "Don Gia", "Thanh Tien"};
        detailTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        detailTable = new JTable(detailTableModel);
        detailTable.setFont(UIConstants.FONT_REGULAR);
        detailTable.setRowHeight(30);
        
        JTableHeader header = detailTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(200, 0));
        
        JLabel actionTitle = new JLabel("Thao Tac");
        actionTitle.setFont(UIConstants.FONT_SUBTITLE);
        actionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(actionTitle);
        panel.add(Box.createVerticalStrut(20));
        
        ModernButton payButton = new ModernButton("Thanh Toan", UIConstants.SUCCESS_COLOR);
        payButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        payButton.setMaximumSize(new Dimension(180, 40));
        payButton.addActionListener(e -> markAsPaid());
        panel.add(payButton);
        panel.add(Box.createVerticalStrut(10));
        
        ModernButton deleteButton = new ModernButton("Xoa HĐ", UIConstants.DANGER_COLOR);
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.setMaximumSize(new Dimension(180, 40));
        deleteButton.addActionListener(e -> deleteInvoice());
        panel.add(deleteButton);
        panel.add(Box.createVerticalStrut(20));
        
        JLabel infoLabel = new JLabel("<html><center>Chon hoa don<br>de xem chi tiet</center></html>");
        infoLabel.setFont(UIConstants.FONT_SMALL);
        infoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private void loadInvoices() {
        tableModel.setRowCount(0);
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        
        for (Invoice invoice : invoices) {
            addInvoiceToTable(invoice);
        }
    }
    
    private void addInvoiceToTable(Invoice invoice) {
        Apartment apartment = apartmentDAO.getApartmentById(invoice.getApartmentId());
        String apartmentInfo = "N/A";
        
        if (apartment != null) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            if (floor != null) {
                Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                apartmentInfo = (building != null ? building.getName() : "") + 
                              " - " + apartment.getApartmentNumber();
            }
        }
        
        String monthYear = invoice.getInvoiceMonth() + "/" + invoice.getInvoiceYear();
        String paymentDate = (invoice.getPaymentDate() != null) ? 
            dateFormat.format(invoice.getPaymentDate()) : "";
        
        Object[] row = {
            invoice.getId(),
            apartmentInfo,
            monthYear,
            invoice.getTotalAmount(),
            invoice.getPaymentStatus(),
            paymentDate
        };
        tableModel.addRow(row);
    }
    
    private void loadSelectedInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow < 0) {
            detailTableModel.setRowCount(0);
            selectedInvoice = null;
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        selectedInvoice = invoiceDAO.getInvoiceById(id);
        
        // Load details (simplified - in real app, use InvoiceDetailDAO)
        detailTableModel.setRowCount(0);
        
        if (selectedInvoice != null) {
            // Example details - in real app, fetch from invoice_details table
            // For now, show summary
            Object[] row = {
                "Tien thue + Dich vu",
                "1",
                selectedInvoice.getTotalAmount(),
                selectedInvoice.getTotalAmount()
            };
            detailTableModel.addRow(row);
        }
    }
    
    private void filterInvoices() {
        int month = (Integer) monthCombo.getSelectedItem();
        int year = (Integer) yearCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        List<Invoice> invoices;
        if ("All".equals(status)) {
            invoices = invoiceDAO.getInvoicesByMonth(month, year);
        } else {
            invoices = invoiceDAO.getInvoicesByMonth(month, year);
            // Filter by status
            invoices.removeIf(inv -> !inv.getPaymentStatus().equals(status));
        }
        
        for (Invoice invoice : invoices) {
            addInvoiceToTable(invoice);
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Khong tim thay hoa don nao!", 
                "Thong Bao", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void markAsPaid() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon hoa don can thanh toan!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ("Paid".equals(selectedInvoice.getPaymentStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Hoa don nay da duoc thanh toan!", 
                "Thong Bao", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xac nhan da thanh toan hoa don nay?",
            "Xac Nhan",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            selectedInvoice.setPaymentStatus("Paid");
            selectedInvoice.setPaymentDate(new Date());
            
            if (invoiceDAO.updateInvoice(selectedInvoice)) {
                JOptionPane.showMessageDialog(this, 
                    "Da danh dau thanh toan!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadInvoices();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cap nhat that bai!", 
                    "Loi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteInvoice() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui long chon hoa don can xoa!", 
                "Canh Bao", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa hoa don nay?",
            "Xac Nhan Xoa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (invoiceDAO.deleteInvoice(selectedInvoice.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Xoa hoa don thanh cong!", 
                    "Thanh Cong", 
                    JOptionPane.INFORMATION_MESSAGE);
                selectedInvoice = null;
                detailTableModel.setRowCount(0);
                loadInvoices();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xoa that bai!", 
                    "Loi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}