package view;

import dao.BuildingDAO;
import model.Building;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Màn hình Quản lý Tòa nhà
 * - Thiết kế: Modern Standard (Bo góc 15px)
 * - Đồng bộ với giao diện Card bên dưới.
 */
public class BuildingManagementPanel extends JPanel {
    
    private BuildingDAO buildingDAO;
    private JPanel cardsContainer; 
    private JTextField searchField;
    
    public BuildingManagementPanel() {
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 30, 20, 30));
        
        createHeader();
        createContentArea();
        
        loadBuildings(); // Load dữ liệu ban đầu
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // 1. TIÊU ĐỀ
        JLabel titleLabel = new JLabel("Danh sách Tòa nhà / Chung cư");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        // 2. KHU VỰC TÁC VỤ (Action Panel)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // A. Ô TÌM KIẾM (Bo góc 15px - Đồng bộ với Card)
        searchField = new RoundedSearchField("Tìm tên hoặc địa chỉ...", 15);
        searchField.setPreferredSize(new Dimension(320, 40)); // Chiều cao 40px chuẩn
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);

        // Tính năng Live Search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { search(); }
            @Override public void removeUpdate(DocumentEvent e) { search(); }
            @Override public void changedUpdate(DocumentEvent e) { search(); }
            private void search() { loadBuildings(searchField.getText()); }
        });
        
        // B. NÚT THÊM MỚI (Bo góc 15px - Đồng bộ)
        JButton btnAdd = new RoundedButton(" Thêm Tòa Nhà Mới", 15);
        btnAdd.setIcon(new SimpleIcon("PLUS", 14, Color.WHITE)); // Icon dấu cộng nhỏ gọn
        btnAdd.setPreferredSize(new Dimension(200, 40)); // Kích thước bằng ô tìm kiếm
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> showAddDialog());
        
        // Thêm vào panel (Khoảng cách 15px giữa 2 phần tử)
        actionPanel.add(searchField);
        actionPanel.add(Box.createHorizontalStrut(15)); 
        actionPanel.add(btnAdd);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createContentArea() {
        // Wrapper để tránh card bị giãn khi ít phần tử
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        cardsContainer = new JPanel(new GridLayout(0, 2, 20, 20)); 
        cardsContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        cardsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        wrapperPanel.add(cardsContainer, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); // Cuộn mượt hơn
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadBuildings() {
        loadBuildings(null);
    }

    private void loadBuildings(String keyword) {
        cardsContainer.removeAll();
        
        List<Building> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = buildingDAO.getAllBuildings();
        } else {
            list = buildingDAO.searchBuildingsByName(keyword.trim());
        }
        
        for (Building b : list) {
            BuildingDAO.BuildingStats stats = buildingDAO.getBuildingStatistics(b.getId());
            BuildingCard card = new BuildingCard(b, stats, 
                this::showEditDialog, 
                this::deleteBuilding
            );
            cardsContainer.add(card);
        }
        
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }
    
    // --- CÁC HÀM CRUD ---
    private void showAddDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BuildingDialog dialog = new BuildingDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (buildingDAO.addBuilding(dialog.getBuilding())) {
                JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                loadBuildings();
            }
        }
    }
    
    private void showEditDialog(Building building) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BuildingDialog dialog = new BuildingDialog(parent, building);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (buildingDAO.updateBuilding(dialog.getBuilding())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadBuildings();
            }
        }
    }
    
    private void deleteBuilding(Building building) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa tòa nhà: " + building.getName() + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (buildingDAO.deleteBuilding(building.getId())) {
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =================================================================
    // 1. CUSTOM: Ô TÌM KIẾM BO GÓC (Đồng bộ style Card)
    // =================================================================
    private static class RoundedSearchField extends JTextField {
        private String placeholder;
        private int arc;
        public RoundedSearchField(String placeholder, int arc) {
            this.placeholder = placeholder;
            this.arc = arc;
            setOpaque(false);
            // Padding vừa phải, không quá sâu
            setBorder(new EmptyBorder(5, 15, 5, 15)); 
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Nền trắng
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            
            // Viền xám nhạt (Màu này giống viền Card BuildingCard)
            g2.setColor(new Color(220, 220, 220)); 
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            
            super.paintComponent(g); // Vẽ text

            // Placeholder
            if (getText().isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.drawString(placeholder, getInsets().left, (getHeight() + g2.getFontMetrics().getAscent())/2 - 2);
            }
            g2.dispose();
        }
    }

    // =================================================================
    // 2. CUSTOM: NÚT BẤM BO GÓC (Đồng bộ style)
    // =================================================================
    private static class RoundedButton extends JButton {
        private int arc;

        public RoundedButton(String text, int arc) {
            super(text);
            this.arc = arc;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Hiệu ứng nhấn
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }

            // Vẽ nền bo tròn
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            
            super.paintComponent(g); 
        }
    }

    // --- CLASS VẼ ICON (PLUS) ---
    private static class SimpleIcon implements Icon {
        private String type;
        private int size;
        private Color color;
        public SimpleIcon(String type, int size, Color color) {
            this.type = type; this.size = size; this.color = color;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f)); // Nét mảnh hơn chút cho tinh tế
            g2.translate(x, y);
            if ("PLUS".equals(type)) {
                int m = size / 2;
                g2.drawLine(2, m, size-2, m); // Ngang
                g2.drawLine(m, 2, m, size-2); // Dọc
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}