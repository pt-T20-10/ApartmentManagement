package view;

import dao.BuildingDAO;
import dao.FloorDAO;
import model.Building;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

public class FloorManagementPanel extends JPanel {

    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JPanel cardsContainer;
    private Building currentBuilding; 
    
    // Dropdown chọn tòa nhà
    private JComboBox<Building> cbbBuilding;
    private JLabel titleLabel; // Để cập nhật tiêu đề khi chọn combobox

    // Constructor mặc định cho Dashboard gọi
    public FloorManagementPanel() {
        this(null);
    }

    public FloorManagementPanel(Building building) {
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        this.currentBuilding = building; // Có thể null ban đầu

        initUI();
        loadBuildingData(); // Load danh sách tòa nhà vào ComboBox
    }
    
    // Hàm này để Dashboard gọi khi muốn set tòa nhà từ bên ngoài
    public void setBuilding(Building building) {
        this.currentBuilding = building;
        // Cập nhật ComboBox cho khớp (nếu cần)
        if (building != null) {
            cbbBuilding.setSelectedItem(building);
        }
        loadFloors();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // --- Left Side: Label + Dropdown ---
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel lblFilter = new JLabel("Quản Lý Tầng:");
        lblFilter.setFont(UIConstants.FONT_TITLE);
        lblFilter.setForeground(UIConstants.TEXT_PRIMARY);
        
        cbbBuilding = new JComboBox<>();
        cbbBuilding.setPreferredSize(new Dimension(280, 35));
        cbbBuilding.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbBuilding.setBackground(Color.WHITE);
        
        // --- QUAN TRỌNG: SỬA LỖI HIỂN THỊ COMBOBOX ---
        cbbBuilding.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Building) {
                    setText(((Building) value).getName()); // Chỉ hiện tên
                }
                return this;
            }
        });

        // Sự kiện chọn tòa nhà
        cbbBuilding.addActionListener(e -> {
            Building selected = (Building) cbbBuilding.getSelectedItem();
            if (selected != null && selected.getId() != null) { // Bỏ qua item giả
                this.currentBuilding = selected;
                loadFloors();
            } else {
                this.currentBuilding = null;
                cardsContainer.removeAll();
                cardsContainer.repaint();
            }
        });

        leftHeader.add(lblFilter);
        leftHeader.add(cbbBuilding);

        // --- Right Side: Nút Thêm ---
        JButton btnAdd = new RoundedButton(" Thêm Tầng Mới", 15);
        btnAdd.setIcon(new SimpleIcon("PLUS", 14, Color.WHITE));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> showAddDialog());

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // === 2. CONTENT ===
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        cardsContainer = new JPanel(new GridLayout(0, 3, 20, 20));
        cardsContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        cardsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));

        wrapperPanel.add(cardsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadBuildingData() {
        List<Building> buildings = buildingDAO.getAllBuildings();
        cbbBuilding.removeAllItems();
        
        if (buildings.isEmpty()) {
            cbbBuilding.addItem(new Building(null, "Chưa có tòa nhà nào", "", "", "", false));
        } else {
            for (Building b : buildings) {
                cbbBuilding.addItem(b);
            }
            // Nếu có currentBuilding truyền vào thì chọn nó, không thì chọn cái đầu
            if (currentBuilding != null) {
                // Tìm building trong list để set selected (vì object reference có thể khác nhau)
                for(int i=0; i<cbbBuilding.getItemCount(); i++) {
                    Building b = cbbBuilding.getItemAt(i);
                    if(b.getId() != null && b.getId().equals(currentBuilding.getId())) {
                        cbbBuilding.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                cbbBuilding.setSelectedIndex(0);
            }
        }
    }

    public void loadFloors() {
        cardsContainer.removeAll();
        
        if (currentBuilding != null && currentBuilding.getId() != null) {
            List<Floor> list = floorDAO.getFloorsByBuildingId(currentBuilding.getId());
            
            if (list.isEmpty()) {
                showEmptyMessage("Tòa nhà này chưa có tầng nào.");
            } else {
                for (Floor f : list) {
                    FloorDAO.FloorStats stats = floorDAO.getFloorStatistics(f.getId());
                    FloorCard card = new FloorCard(f, stats);
                    cardsContainer.add(card);
                }
            }
        } else {
            showEmptyMessage("Vui lòng chọn một tòa nhà.");
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }
    
    private void showEmptyMessage(String msg) {
        JLabel guideLabel = new JLabel(msg);
        guideLabel.setHorizontalAlignment(SwingConstants.CENTER);
        guideLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        guideLabel.setForeground(Color.GRAY);
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        msgPanel.add(guideLabel, BorderLayout.CENTER);
        cardsContainer.add(msgPanel);
    }
    
    // --- XỬ LÝ DIALOG THÊM / SỬA ---
    private void showAddDialog() {
        if (currentBuilding == null || currentBuilding.getId() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tòa nhà trước khi thêm tầng!");
            return;
        }
        
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        // Tạo tầng mới gán sẵn ID tòa nhà hiện tại
        Floor newFloor = new Floor();
        newFloor.setBuildingId(currentBuilding.getId());
        
        FloorDialog dialog = new FloorDialog(parent, newFloor);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (floorDAO.insertFloor(dialog.getFloor())) {
                JOptionPane.showMessageDialog(this, "Thêm tầng thành công!");
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        }
    }

    private void editFloor(Floor floor) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        FloorDialog dialog = new FloorDialog(parent, floor);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (floorDAO.updateFloor(dialog.getFloor())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        }
    }

    private void deleteFloor(Floor floor) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa " + floor.getName() + "?", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (floorDAO.deleteFloor(floor.getId())) {
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

    // =================================================================
    // INNER CLASSES (Card, Bar, Icons...) - GIỮ NGUYÊN
    // =================================================================
    private class FloorCard extends JPanel {
        private Floor floor;
        private FloorDAO.FloorStats stats;
        public FloorCard(Floor floor, FloorDAO.FloorStats stats) {
            this.floor = floor; this.stats = stats; setOpaque(false); setPreferredSize(new Dimension(300, 160)); initCardUI();
        }
        private void initCardUI() {
            setLayout(new BorderLayout()); setBorder(new EmptyBorder(15, 20, 15, 20));
            JPanel topPanel = new JPanel(new BorderLayout()); topPanel.setOpaque(false);
            JLabel lblName = new JLabel(floor.getName()); lblName.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblName.setForeground(new Color(33, 33, 33));
            topPanel.add(lblName, BorderLayout.WEST);
            JLabel lblTotal = new JLabel("Tổng số căn: " + stats.totalApartments); lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblTotal.setForeground(Color.GRAY); lblTotal.setBorder(new EmptyBorder(5, 0, 10, 0));
            OccupancyBar progressBar = new OccupancyBar(stats.rentedApartments, stats.totalApartments);
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actionPanel.setOpaque(false); actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            JButton btnEdit = createIconButton("EDIT", new Color(100, 149, 237)); btnEdit.addActionListener(e -> editFloor(floor));
            JButton btnDelete = createIconButton("DELETE", new Color(239, 83, 80)); btnDelete.addActionListener(e -> deleteFloor(floor));
            actionPanel.add(btnEdit); actionPanel.add(btnDelete);
            JPanel centerContent = new JPanel(); centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS)); centerContent.setOpaque(false);
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT); lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT); progressBar.setAlignmentX(Component.LEFT_ALIGNMENT); actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerContent.add(lblName); centerContent.add(lblTotal); centerContent.add(progressBar); centerContent.add(actionPanel);
            add(centerContent, BorderLayout.CENTER);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            g2.setColor(new Color(220, 220, 220)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15); g2.dispose(); super.paintComponent(g);
        }
        private JButton createIconButton(String iconType, Color color) {
            JButton btn = new JButton(new SimpleIcon(iconType, 16, color)); btn.setPreferredSize(new Dimension(32, 32)); btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(0,0,0,0) { @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(230, 230, 230)); g2.drawRoundRect(x, y, width-1, height-1, 8, 8); g2.dispose(); } }); btn.setBorderPainted(true); return btn;
        }
    }
    private class OccupancyBar extends JPanel {
        int rented, total;
        public OccupancyBar(int rented, int total) { this.rented = rented; this.total = total; setPreferredSize(new Dimension(200, 25)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 8; int percent = (total == 0) ? 0 : (rented * 100 / total); int blueW = (int)(w * (percent / 100.0));
            g2.setColor(new Color(165, 214, 167)); g2.fillRoundRect(0, 0, w, h, arc, arc);
            if (blueW > 0) { g2.setColor(new Color(33, 150, 243)); if (percent == 100) g2.fillRoundRect(0, 0, w, h, arc, arc); else { g2.fillRoundRect(0, 0, blueW, h, arc, arc); g2.fillRect(blueW - arc, 0, arc, h); } }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); FontMetrics fm = g2.getFontMetrics();
            if (blueW > 50) { String txt = rented + " Đang thuê"; g2.setColor(Color.WHITE); g2.drawString(txt, 10, (h + fm.getAscent()) / 2 - 2); }
            if (w - blueW > 40) { String txt = (total - rented) + " Trống"; g2.setColor(new Color(27, 94, 32)); g2.drawString(txt, w - fm.stringWidth(txt) - 10, (h + fm.getAscent()) / 2 - 2); } g2.dispose();
        }
    }
    private static class SimpleIcon implements Icon {
        private String type; private int size; private Color color; public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.translate(x, y);
            if ("EDIT".equals(type)) { g2.rotate(Math.toRadians(45), size/2.0, size/2.0); g2.drawRoundRect(size/2-2, 0, 4, size-4, 1, 1); g2.drawLine(size/2-2, 3, size/2+2, 3); Path2D tip = new Path2D.Float(); tip.moveTo(size/2-2, size-4); tip.lineTo(size/2, size); tip.lineTo(size/2+2, size-4); g2.fill(tip); }
            else if ("DELETE".equals(type)) { int w = size-6; int h = size-4; int mx = 3; int my = 4; g2.drawRoundRect(mx, my, w, h, 3, 3); g2.drawLine(1, my, size-1, my); g2.drawArc(size/2-2, 0, 4, 4, 0, 180); g2.drawLine(size/2-2, my+3, size/2-2, my+h-3); g2.drawLine(size/2+2, my+3, size/2+2, my+h-3); }
            else if ("PLUS".equals(type)) { g2.setStroke(new BasicStroke(2.0f)); g2.drawLine(0, size/2, size, size/2); g2.drawLine(size/2, 0, size/2, size); } g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
    private static class RoundedButton extends JButton {
        private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); }
    }
}