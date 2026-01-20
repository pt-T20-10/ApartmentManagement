package view;

import dao.ServiceDAO;
import model.Service;
import util.UIConstants;
import util.ModernButton;
import util.MoneyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Service Management Panel - REDESIGNED
 * Modern interface with VND formatting, placeholder search
 */
public class ServiceManagementPanel extends JPanel {
    
    private ServiceDAO serviceDAO;
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public ServiceManagementPanel() {
        this.serviceDAO = new ServiceDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadServices();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("⚡");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(new Color(255, 193, 7));
        
        JLabel titleLabel = new JLabel("Quản Lý Dịch Vụ");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Search panel with placeholder
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Placeholder
        final String PLACEHOLDER = "Tìm theo tên dịch vụ...";
        final Color PLACEHOLDER_COLOR = new Color(158, 158, 158);
        final Color TEXT_COLOR = new Color(33, 33, 33);
        
        searchField.setText(PLACEHOLDER);
        searchField.setForeground(PLACEHOLDER_COLOR);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals(PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(PLACEHOLDER);
                    searchField.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
        
        searchField.addActionListener(e -> searchServices());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchServices());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText(PLACEHOLDER);
            searchField.setForeground(PLACEHOLDER_COLOR);
            loadServices();
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
        
        // Table columns - NO Description
        String[] columns = {"ID", "Tên Dịch Vụ", "Đơn Vị", "Đơn Giá", "Bắt Buộc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        serviceTable = new JTable(tableModel);
        serviceTable.setFont(UIConstants.FONT_REGULAR);
        serviceTable.setRowHeight(50);
        serviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceTable.setShowGrid(true);
        serviceTable.setGridColor(new Color(240, 240, 240));
        serviceTable.setSelectionBackground(new Color(232, 240, 254));
        serviceTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        
        // Double-click to edit
        serviceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editService();
                }
            }
        });
        
        // Table header styling
        JTableHeader header = serviceTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Center align header
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        // Column widths
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        serviceTable.getColumnModel().getColumn(0).setMaxWidth(80);
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        serviceTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        // Center align ALL columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < serviceTable.getColumnCount(); i++) {
            serviceTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Override with special renderer for "Bắt Buộc" column (keep center alignment)
        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                label.setHorizontalAlignment(SwingConstants.CENTER); // CENTER!
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if ("Có".equals(value)) {
                    label.setText("✓ Có");
                    label.setForeground(new Color(22, 163, 74)); // Green
                } else {
                    label.setText("○ Không");
                    label.setForeground(new Color(156, 163, 175)); // Gray
                }
                
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(serviceTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Dịch Vụ", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(160, 45));
        addButton.addActionListener(e -> addService());
        
        ModernButton editButton = new ModernButton("✏ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editService());
        
        ModernButton deleteButton = new ModernButton("🗑 Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteService());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadServices() {
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        
        for (Service service : services) {
            Object[] row = {
                service.getId(),
                service.getName(),
                service.getUnit(),
                MoneyFormatter.formatMoney(service.getUnitPrice()) + " đ", // VND formatting
                service.isMandatory() ? "Có" : "Không"
            };
            tableModel.addRow(row);
        }
    }
    
    private void addService() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ServiceDialog dialog = new ServiceDialog(parentFrame);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Service service = dialog.getService();
            
            if (serviceDAO.insertService(service)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm dịch vụ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm dịch vụ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editService() {
        int selectedRow = serviceTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn dịch vụ cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Service service = serviceDAO.getServiceById(id);
        
        if (service == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy dịch vụ!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ServiceDialog dialog = new ServiceDialog(parentFrame, service);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Service updatedService = dialog.getService();
            
            if (serviceDAO.updateService(updatedService)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật dịch vụ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật dịch vụ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteService() {
        int selectedRow = serviceTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn dịch vụ cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String serviceName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa dịch vụ '" + serviceName + "'?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (serviceDAO.deleteService(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa dịch vụ thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa dịch vụ thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchServices() {
        String keyword = searchField.getText().trim().toLowerCase();
        
        // Ignore placeholder
        if (keyword.isEmpty() || keyword.equals("tìm theo tên dịch vụ...")) {
            loadServices();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        
        for (Service service : services) {
            if (service.getName().toLowerCase().contains(keyword) ||
                service.getUnit().toLowerCase().contains(keyword)) {
                
                Object[] row = {
                    service.getId(),
                    service.getName(),
                    service.getUnit(),
                    MoneyFormatter.formatMoney(service.getUnitPrice()) + " đ",
                    service.isMandatory() ? "Có" : "Không"
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy dịch vụ nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}