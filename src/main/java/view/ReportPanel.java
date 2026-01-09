package view;

import dao.*;
import model.*;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;

/**
 * Report Panel
 * Statistics and reports dashboard
 */
public class ReportPanel extends JPanel {
    
    private InvoiceDAO invoiceDAO;
    private ApartmentDAO apartmentDAO;
    private ContractDAO contractDAO;
    private ResidentDAO residentDAO;
    private BuildingDAO buildingDAO;
    
    private JComboBox<Integer> fromMonthCombo;
    private JComboBox<Integer> toMonthCombo;
    private JComboBox<Integer> yearCombo;
    
    private NumberFormat currencyFormat;
    
    public ReportPanel() {
        this.invoiceDAO = new InvoiceDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.contractDAO = new ContractDAO();
        this.residentDAO = new ResidentDAO();
        this.buildingDAO = new BuildingDAO();
        
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createMainPanel();
        
        loadStatistics();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[P]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Bao Cao Thong Ke");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        
        filterPanel.add(new JLabel("Tu thang:"));
        fromMonthCombo = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            fromMonthCombo.addItem(i);
        }
        fromMonthCombo.setSelectedItem(1);
        filterPanel.add(fromMonthCombo);
        
        filterPanel.add(new JLabel("Den thang:"));
        toMonthCombo = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            toMonthCombo.addItem(i);
        }
        toMonthCombo.setSelectedItem(currentMonth);
        filterPanel.add(toMonthCombo);
        
        filterPanel.add(new JLabel("Nam:"));
        yearCombo = new JComboBox<>();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.setSelectedItem(currentYear);
        filterPanel.add(yearCombo);
        
        ModernButton refreshButton = new ModernButton("[R] Cap Nhat", UIConstants.PRIMARY_COLOR);
        refreshButton.addActionListener(e -> loadStatistics());
        filterPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Top: Statistics cards
        JPanel statsPanel = createStatisticsPanel();
        
        // Bottom: Tables
        JPanel tablesPanel = createTablesPanel();
        
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(tablesPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Row 1
        panel.add(createStatCard("Tong Toa Nha", "", UIConstants.PRIMARY_COLOR, "buildings"));
        panel.add(createStatCard("Tong Can Ho", "", UIConstants.INFO_COLOR, "apartments"));
        panel.add(createStatCard("Tong Cu Dan", "", UIConstants.SUCCESS_COLOR, "residents"));
        panel.add(createStatCard("Hop Dong Active", "", UIConstants.WARNING_COLOR, "contracts"));
        
        // Row 2
        panel.add(createStatCard("Doanh Thu Thang", "", UIConstants.SUCCESS_COLOR, "revenue"));
        panel.add(createStatCard("HĐ Da Thanh Toan", "", UIConstants.PRIMARY_COLOR, "paid"));
        panel.add(createStatCard("HĐ Chua TT", "", UIConstants.WARNING_COLOR, "pending"));
        panel.add(createStatCard("Ty Le Lap Day", "", UIConstants.INFO_COLOR, "occupancy"));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color, String type) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_REGULAR);
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Store for later update
        valueLabel.setName(type);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Left: Revenue by month
        panel.add(createRevenueTable());
        
        // Right: Apartment status
        panel.add(createApartmentStatusTable());
        
        return panel;
    }
    
    private JPanel createRevenueTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Doanh Thu Theo Thang"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"Thang", "Doanh Thu", "Da Thu", "Chua Thu"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UIConstants.FONT_HEADING);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store model for later update
        panel.putClientProperty("tableModel", model);
        
        return panel;
    }
    
    private JPanel createApartmentStatusTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Trang Thai Can Ho Theo Toa"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"Toa Nha", "Tong", "Da Thue", "Con Trong", "Bao Tri"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UIConstants.FONT_HEADING);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store model for later update
        panel.putClientProperty("tableModel", model);
        
        return panel;
    }
    
    private void loadStatistics() {
        Calendar cal = Calendar.getInstance();
        int currentYear = (Integer) yearCombo.getSelectedItem();
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        
        // Update stat cards
        updateStatCard("buildings", String.valueOf(buildingDAO.countBuildings()));
        updateStatCard("apartments", String.valueOf(apartmentDAO.countApartments()));
        updateStatCard("residents", String.valueOf(residentDAO.countResidents()));
        updateStatCard("contracts", String.valueOf(contractDAO.countActiveContracts()));
        
        // Revenue
        BigDecimal monthlyRevenue = invoiceDAO.getMonthlyRevenue(currentMonth, currentYear);
        updateStatCard("revenue", formatCurrency(monthlyRevenue));
        
        // Invoice stats
        List<Invoice> allInvoices = invoiceDAO.getInvoicesByMonth(currentMonth, currentYear);
        long paidCount = allInvoices.stream().filter(i -> "Paid".equals(i.getPaymentStatus())).count();
        long pendingCount = allInvoices.stream().filter(i -> !"Paid".equals(i.getPaymentStatus())).count();
        
        updateStatCard("paid", String.valueOf(paidCount));
        updateStatCard("pending", String.valueOf(pendingCount));
        
        // Occupancy rate
        int totalApt = apartmentDAO.countApartments();
        int occupiedApt = apartmentDAO.countApartmentsByStatus("Occupied");
        double occupancyRate = totalApt > 0 ? (occupiedApt * 100.0 / totalApt) : 0;
        updateStatCard("occupancy", String.format("%.1f%%", occupancyRate));
        
        // Update tables
        loadRevenueTable();
        loadApartmentStatusTable();
    }
    
    private void updateStatCard(String type, String value) {
        Component[] components = getComponents();
        updateStatCardRecursive(components, type, value);
    }
    
    private void updateStatCardRecursive(Component[] components, String type, String value) {
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (type.equals(label.getName())) {
                    label.setText(value);
                    return;
                }
            }
            if (comp instanceof Container) {
                updateStatCardRecursive(((Container) comp).getComponents(), type, value);
            }
        }
    }
    
    private void loadRevenueTable() {
        int year = (Integer) yearCombo.getSelectedItem();
        int fromMonth = (Integer) fromMonthCombo.getSelectedItem();
        int toMonth = (Integer) toMonthCombo.getSelectedItem();
        
        // Find the revenue table
        Component[] components = getComponents();
        DefaultTableModel model = findTableModel(components, "Doanh Thu Theo Thang");
        
        if (model != null) {
            model.setRowCount(0);
            
            for (int month = fromMonth; month <= toMonth; month++) {
                List<Invoice> invoices = invoiceDAO.getInvoicesByMonth(month, year);
                
                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal paidRevenue = BigDecimal.ZERO;
                BigDecimal pendingRevenue = BigDecimal.ZERO;
                
                for (Invoice invoice : invoices) {
                    totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                    if ("Paid".equals(invoice.getPaymentStatus())) {
                        paidRevenue = paidRevenue.add(invoice.getTotalAmount());
                    } else {
                        pendingRevenue = pendingRevenue.add(invoice.getTotalAmount());
                    }
                }
                
                Object[] row = {
                    "T" + month + "/" + year,
                    formatCurrency(totalRevenue),
                    formatCurrency(paidRevenue),
                    formatCurrency(pendingRevenue)
                };
                model.addRow(row);
            }
        }
    }
    
    private void loadApartmentStatusTable() {
        // Find the apartment status table
        Component[] components = getComponents();
        DefaultTableModel model = findTableModel(components, "Trang Thai Can Ho Theo Toa");
        
        if (model != null) {
            model.setRowCount(0);
            
            List<Building> buildings = buildingDAO.getAllBuildings();
            
            for (Building building : buildings) {
                List<Apartment> apartments = apartmentDAO.getApartmentsByBuildingId(building.getId());
                
                int total = apartments.size();
                int occupied = 0;
                int available = 0;
                int maintenance = 0;
                
                for (Apartment apt : apartments) {
                    switch (apt.getStatus()) {
                        case "Occupied":
                            occupied++;
                            break;
                        case "Available":
                            available++;
                            break;
                        case "Maintenance":
                            maintenance++;
                            break;
                    }
                }
                
                Object[] row = {
                    building.getName(),
                    total,
                    occupied,
                    available,
                    maintenance
                };
                model.addRow(row);
            }
        }
    }
    
    private DefaultTableModel findTableModel(Component[] components, String title) {
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder border = 
                        (javax.swing.border.TitledBorder) panel.getBorder();
                    if (title.equals(border.getTitle())) {
                        Object model = panel.getClientProperty("tableModel");
                        if (model instanceof DefaultTableModel) {
                            return (DefaultTableModel) model;
                        }
                    }
                }
                // Recursive search
                DefaultTableModel result = findTableModel(panel.getComponents(), title);
                if (result != null) return result;
            }
        }
        return null;
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0đ";
        
        // Simple formatting
        long value = amount.longValue();
        if (value >= 1000000000) {
            return String.format("%.1f ty", value / 1000000000.0);
        } else if (value >= 1000000) {
            return String.format("%.1f tr", value / 1000000.0);
        } else if (value >= 1000) {
            return String.format("%.0f k", value / 1000.0);
        } else {
            return value + "đ";
        }
    }
}