package view;

import dao.BuildingDAO;
import dao.FloorDAO;
import model.Building;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class FloorManagementPanel extends JPanel {

    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JPanel cardsContainer;
    private Building currentBuilding; 
    
    private JComboBox<Building> cbbBuilding;
    private JLabel lblMaintenanceWarning;
    private JButton btnBatchAdd;
    private JButton btnAdd;

    public FloorManagementPanel() {
        this(null);
    }

    public FloorManagementPanel(Building building) {
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        this.currentBuilding = building;

        initUI();
        loadBuildingData(); 
    }
    
    public void setBuilding(Building building) {
        this.currentBuilding = building;
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

        // LEFT
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel lblFilter = new JLabel("Quản Lý Tầng:");
        lblFilter.setFont(UIConstants.FONT_TITLE);
        lblFilter.setForeground(UIConstants.TEXT_PRIMARY);
        
        cbbBuilding = new JComboBox<>();
        cbbBuilding.setPreferredSize(new Dimension(280, 35));
        cbbBuilding.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbBuilding.setBackground(Color.WHITE);
        cbbBuilding.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Building) setText(((Building) value).getName());
                return this;
            }
        });
        cbbBuilding.addActionListener(e -> {
            Building selected = (Building) cbbBuilding.getSelectedItem();
            if (selected != null && selected.getId() != null) { 
                this.currentBuilding = selected; loadFloors();
            } else {
                this.currentBuilding = null; cardsContainer.removeAll(); cardsContainer.repaint();
            }
        });
        leftHeader.add(lblFilter); leftHeader.add(cbbBuilding);

        // RIGHT
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setBackground(UIConstants.BACKGROUND_COLOR);

        // --- SỬA LỖI ICON CẢNH BÁO TẠI ĐÂY ---
        lblMaintenanceWarning = new JLabel(" Tòa nhà đang bảo trì - Tạm khóa chỉnh sửa");
        // Dùng HeaderIcon vẽ hình tam giác thay vì ký tự text
        lblMaintenanceWarning.setIcon(new HeaderIcon("WARNING", 16, new Color(211, 47, 47))); 
        lblMaintenanceWarning.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaintenanceWarning.setForeground(new Color(211, 47, 47)); 
        lblMaintenanceWarning.setVisible(false);

        btnBatchAdd = new RoundedButton(" Thêm Hàng Loạt", 15);
        btnBatchAdd.setIcon(new HeaderIcon("LAYER_PLUS", 14, Color.WHITE));
        btnBatchAdd.setPreferredSize(new Dimension(160, 40));
        btnBatchAdd.setBackground(new Color(0, 150, 136));
        btnBatchAdd.setForeground(Color.WHITE);
        btnBatchAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatchAdd.addActionListener(e -> showBatchAddDialog());

        btnAdd = new RoundedButton(" Thêm Tầng Mới", 15);
        btnAdd.setIcon(new HeaderIcon("PLUS", 14, Color.WHITE));
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.addActionListener(e -> showAddDialog());

        rightHeader.add(lblMaintenanceWarning);
        rightHeader.add(btnBatchAdd);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
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
            cbbBuilding.addItem(new Building(null, "Chưa có tòa nhà nào", "", "", "", "Đang hoạt động", null, false));
        } else {
            for (Building b : buildings) cbbBuilding.addItem(b);
            if (currentBuilding != null) {
                for(int i=0; i<cbbBuilding.getItemCount(); i++) {
                    Building b = cbbBuilding.getItemAt(i);
                    if(b.getId() != null && b.getId().equals(currentBuilding.getId())) { cbbBuilding.setSelectedIndex(i); break; }
                }
            } else cbbBuilding.setSelectedIndex(0);
        }
    }

    public void loadFloors() {
        cardsContainer.removeAll();
        if (currentBuilding != null && currentBuilding.getId() != null) {
            List<Floor> list = floorDAO.getFloorsByBuildingId(currentBuilding.getId());
            boolean isMaintenance = "Đang bảo trì".equals(currentBuilding.getStatus());
            updateMaintenanceUI(isMaintenance);
            
            if (list.isEmpty()) showEmptyMessage("Tòa nhà này chưa có tầng nào.");
            else {
                for (Floor f : list) {
                    dao.FloorDAO.FloorStats stats = floorDAO.getFloorStatistics(f.getId());
                    // SỬ DỤNG CLASS FloorCard ĐÃ TÁCH RA (Đảm bảo bạn đã có file FloorCard.java)
                    FloorCard card = new FloorCard(f, stats, isMaintenance, this::editFloor, this::deleteFloor);
                    cardsContainer.add(card);
                }
            }
        } else {
            showEmptyMessage("Vui lòng chọn một tòa nhà.");
            updateMaintenanceUI(false);
        }
        cardsContainer.revalidate(); cardsContainer.repaint();
    }
    
    private void updateMaintenanceUI(boolean isMaintenance) {
        lblMaintenanceWarning.setVisible(isMaintenance);
        btnBatchAdd.setEnabled(!isMaintenance);
        btnAdd.setEnabled(!isMaintenance);
        if(isMaintenance) {
            btnBatchAdd.setBackground(Color.GRAY);
            btnAdd.setBackground(Color.GRAY);
        } else {
            btnBatchAdd.setBackground(new Color(0, 150, 136));
            btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        }
    }
    
    private void showEmptyMessage(String msg) {
        JLabel guideLabel = new JLabel(msg); guideLabel.setHorizontalAlignment(SwingConstants.CENTER); guideLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16)); guideLabel.setForeground(Color.GRAY);
        JPanel msgPanel = new JPanel(new BorderLayout()); msgPanel.setBackground(UIConstants.BACKGROUND_COLOR); msgPanel.add(guideLabel, BorderLayout.CENTER);
        cardsContainer.add(msgPanel);
    }
    
    private boolean checkMaintenance() {
        if (currentBuilding != null && "Đang bảo trì".equals(currentBuilding.getStatus())) {
            JOptionPane.showMessageDialog(this, "Tòa nhà đang bảo trì. Không thể thao tác!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private void showBatchAddDialog() {
        if (checkMaintenance()) return;
        if (currentBuilding == null || currentBuilding.getId() == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn Tòa nhà trước!"); return; }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BatchAddFloorDialog dialog = new BatchAddFloorDialog(parent, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isSuccess()) loadFloors();
    }

    private void showAddDialog() {
        if (checkMaintenance()) return;
        if (currentBuilding == null || currentBuilding.getId() == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn Tòa nhà trước!"); return; }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        Floor newFloor = new Floor(); newFloor.setBuildingId(currentBuilding.getId());
        FloorDialog dialog = new FloorDialog(parent, newFloor); dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (floorDAO.insertFloor(dialog.getFloor())) { JOptionPane.showMessageDialog(this, "Thêm tầng thành công!"); loadFloors(); }
            else JOptionPane.showMessageDialog(this, "Thêm thất bại!");
        }
    }

    private void editFloor(Floor floor) {
        if (checkMaintenance()) return;
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        FloorDialog dialog = new FloorDialog(parent, floor); dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (floorDAO.updateFloor(dialog.getFloor())) { JOptionPane.showMessageDialog(this, "Cập nhật thành công!"); loadFloors(); }
            else JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void deleteFloor(Floor floor) {
        if (checkMaintenance()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa " + floor.getName() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (floorDAO.deleteFloor(floor.getId())) loadFloors();
            else JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }

    // --- Helpers cho Panel (Nút & Icon ở Header) ---
    private static class RoundedButton extends JButton { private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); } @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); } }
    
    private static class HeaderIcon implements Icon {
        private String type; private int size; private Color color; public HeaderIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.setStroke(new BasicStroke(2.0f)); g2.translate(x, y);
            if ("PLUS".equals(type)) { 
                g2.drawLine(0, size/2, size, size/2); g2.drawLine(size/2, 0, size/2, size); 
            }
            else if ("LAYER_PLUS".equals(type)) { 
                int w=size-8; int h=size/4; 
                g2.drawRoundRect(4, size/2-4, w, h, 3,3); g2.drawRoundRect(4, size/2+4, w, h, 3,3); 
                g2.drawLine(size/2, 2, size/2, 10); g2.drawLine(size/2-4, 6, size/2+4, 6); 
            }
            else if ("WARNING".equals(type)) { // --- CODE VẼ TAM GIÁC CẢNH BÁO ---
                int[] xPoints = {size/2, 0, size};
                int[] yPoints = {0, size, size};
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.setColor(Color.WHITE);
                g2.fillRect(size/2 - 1, 4, 2, size - 8); // Dấu chấm than
                g2.fillRect(size/2 - 1, size - 3, 2, 2);
            }
            g2.dispose(); 
        } 
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}