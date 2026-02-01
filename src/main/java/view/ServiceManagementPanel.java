package view;

import dao.ServiceDAO;
import model.Service;
import util.UIConstants;
import util.MoneyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Service Management Panel - Fixed Icons & Placeholder
 */
public class ServiceManagementPanel extends JPanel {
    
    private ServiceDAO serviceDAO;
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Statistics labels
    private JLabel totalServicesLabel;
    private JLabel mandatoryServicesLabel;
    private JLabel optionalServicesLabel;
    
    public ServiceManagementPanel() {
        this.serviceDAO = new ServiceDAO();
        
        setLayout(new BorderLayout(0, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Top container
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        
        topContainer.add(createHeader());
        topContainer.add(Box.createVerticalStrut(20));
        topContainer.add(createStatisticsPanel());
        
        add(topContainer, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
        
        loadServices();
        updateStatistics();
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Title with Icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Icon Tia Sét (Vẽ bằng code)
        JLabel iconLabel = new JLabel(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(251, 191, 36)); // Vàng cam
                int[] xPoints = {x+20, x+15, x+22, x+12, x+17, x+10};
                int[] yPoints = {y+5, y+18, y+18, y+30, y+18, y+18};
                g2.fillPolygon(xPoints, yPoints, 6);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 35; }
            @Override public int getIconHeight() { return 35; }
        });
        
        JLabel titleLabel = new JLabel("Quản Lý Dịch Vụ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // ✅ 1. SỬ DỤNG CUSTOM PLACEHOLDER FIELD
        searchField = new PlaceholderTextField("Tìm theo tên dịch vụ...");
        searchField.setPreferredSize(new Dimension(250, 40));
        searchField.addActionListener(e -> searchServices());
        
        // ✅ 2. FIX LỖI ICON (Vẽ trực tiếp)
        JButton searchButton = createIconButton(new SearchIcon(16, Color.WHITE), new Color(33, 150, 243));
        searchButton.addActionListener(e -> searchServices());
        
        JButton refreshButton = createIconButton(new RefreshIcon(16, Color.WHITE), new Color(34, 197, 94));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadServices();
            updateStatistics();
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    // ... (Giữ nguyên phần Statistics và TablePanel như cũ) ...
    // ... Bạn copy lại phần createStatisticsPanel và createTablePanel từ file cũ vào đây ...
    // ... Để tiết kiệm không gian mình chỉ paste những phần thay đổi quan trọng ...

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        totalServicesLabel = new JLabel("0");
        JPanel card1 = createStatCard("Tổng Dịch Vụ", totalServicesLabel, new Color(99, 102, 241));
        
        mandatoryServicesLabel = new JLabel("0");
        JPanel card2 = createStatCard("Dịch Vụ Bắt Buộc", mandatoryServicesLabel, new Color(34, 197, 94));
        
        optionalServicesLabel = new JLabel("0");
        JPanel card3 = createStatCard("Dịch Vụ Tùy Chọn", optionalServicesLabel, new Color(156, 163, 175));
        
        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        
        return statsPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 8)); // Shadow
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                g2d.setColor(Color.WHITE); // BG
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        
        // Icon tròn đơn giản thay cho emoji
        JLabel iconLabel = new JLabel(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fillOval(x, y, 48, 48);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x+14, y+14, 20, 20); // Circle icon
                g2.dispose();
            }
            @Override public int getIconWidth() { return 48; }
            @Override public int getIconHeight() { return 48; }
        });
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        String[] columns = {"ID", "Tên Dịch Vụ", "Đơn Vị", "Đơn Giá", "Bắt Buộc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        serviceTable = new JTable(tableModel);
        serviceTable.setFont(UIConstants.FONT_REGULAR);
        serviceTable.setRowHeight(50);
        serviceTable.setShowGrid(true);
        serviceTable.setGridColor(new Color(240, 240, 240));
        
        // Header styling
        JTableHeader header = serviceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(new Color(75, 85, 99));
        header.setPreferredSize(new Dimension(0, 45));
        
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Columns setup
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        serviceTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        serviceTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Price Right Align
        serviceTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(new Color(22, 163, 74));
                setBorder(new EmptyBorder(0, 0, 0, 15));
                return c;
            }
        });
        
        // Badge Renderer
        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                
                JLabel badge = new JLabel();
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setBorder(new EmptyBorder(4, 12, 4, 12));
                badge.setOpaque(true);
                
                if ("Có".equals(value)) {
                    badge.setText("Bắt buộc");
                    badge.setBackground(new Color(254, 243, 199));
                    badge.setForeground(new Color(180, 83, 9));
                } else {
                    badge.setText("Tùy chọn");
                    badge.setBackground(new Color(243, 244, 246));
                    badge.setForeground(new Color(107, 114, 128));
                }
                panel.add(badge);
                return panel;
            }
        });
        
        serviceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) editService();
            }
        });

        JScrollPane scrollPane = new JScrollPane(serviceTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton addButton = createStyledButton("Thêm Dịch Vụ", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addService());
        
        JButton editButton = createStyledButton("Sửa", UIConstants.WARNING_COLOR);
        editButton.addActionListener(e -> editService());
        
        JButton deleteButton = createStyledButton("Xóa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteService());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        return actionPanel;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE); // White text always safe
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JButton createIconButton(Icon icon, Color bg) {
        JButton btn = new JButton(icon);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(42, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ===== LOGIC METHODS (Giữ nguyên) =====
    private void loadServices() {
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        for (Service service : services) {
            tableModel.addRow(new Object[]{
                service.getId(), service.getName(), service.getUnit(),
                MoneyFormatter.formatMoney(service.getUnitPrice()) + " đ",
                service.isMandatory() ? "Có" : "Không"
            });
        }
    }
    
    private void updateStatistics() {
        List<Service> services = serviceDAO.getAllServices();
        int total = services.size();
        int mandatory = (int) services.stream().filter(Service::isMandatory).count();
        totalServicesLabel.setText(String.valueOf(total));
        mandatoryServicesLabel.setText(String.valueOf(mandatory));
        optionalServicesLabel.setText(String.valueOf(total - mandatory));
    }
    
    private void addService() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ServiceDialog dialog = new ServiceDialog(parent);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (serviceDAO.insertService(dialog.getService())) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadServices(); updateStatistics();
            }
        }
    }
    
    private void editService() {
        int row = serviceTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ!"); return; }
        Long id = (Long) tableModel.getValueAt(row, 0);
        Service s = serviceDAO.getServiceById(id);
        
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ServiceDialog dialog = new ServiceDialog(parent, s);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (serviceDAO.updateService(dialog.getService())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadServices(); updateStatistics();
            }
        }
    }
    
    private void deleteService() {
        int row = serviceTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ!"); return; }
        Long id = (Long) tableModel.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            if (serviceDAO.deleteService(id)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadServices(); updateStatistics();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa dịch vụ này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchServices() {
        String key = searchField.getText().trim().toLowerCase();
        if (key.isEmpty() || key.equals("tìm theo tên dịch vụ...")) { loadServices(); return; }
        tableModel.setRowCount(0);
        for (Service s : serviceDAO.getAllServices()) {
            if (s.getName().toLowerCase().contains(key)) {
                tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getUnit(),
                    MoneyFormatter.formatMoney(s.getUnitPrice()) + " đ",
                    s.isMandatory() ? "Có" : "Không"
                });
            }
        }
    }

    // ===== CUSTOM CLASSES: ICONS & PLACEHOLDER =====
    
    // Class Text Field có Placeholder
    private static class PlaceholderTextField extends JTextField {
        private String placeholder;
        
        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(5, 10, 5, 10)
            ));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(156, 163, 175)); // Text gray
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int padding = (getHeight() - getFontMetrics(getFont()).getAscent()) / 2;
                g2.drawString(placeholder, 10, getHeight() - padding - 2);
                g2.dispose();
            }
        }
    }
    
    // Icon Kính Lúp
    private static class SearchIcon implements Icon {
        int size; Color color;
        public SearchIcon(int size, Color color) { this.size = size; this.color = color; }
        public int getIconWidth() { return size; } public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            int r = size - 5;
            g2.drawOval(x, y, r, r);
            g2.drawLine(x + r, y + r, x + size, y + size);
            g2.dispose();
        }
    }
    
    // Icon Mũi Tên Xoay
    private static class RefreshIcon implements Icon {
        int size; Color color;
        public RefreshIcon(int size, Color color) { this.size = size; this.color = color; }
        public int getIconWidth() { return size; } public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(x+2, y+2, size-4, size-4, 45, 270);
            g2.drawLine(x+size/2+2, y, x+size/2+2, y+5); // Arrow head attempt
            g2.dispose();
        }
    }
}