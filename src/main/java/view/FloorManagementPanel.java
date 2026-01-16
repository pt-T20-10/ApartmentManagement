package view;

import dao.BuildingDAO;
import dao.FloorDAO;
import model.Building;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

public class FloorManagementPanel extends JPanel {

    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    private JPanel cardsContainer;
    private Building currentBuilding; 
    
    private JComboBox<Building> cbbBuilding;
    private JPanel overlayPanel; 
    private JButton btnBatchAdd;
    private JButton btnAdd;
    private Consumer<Floor> onFloorSelect;
    
    // *** THÊM DÒNG NÀY - QUAN TRỌNG ***
    private SwingWorker<?, ?> currentWorker = null;

    public FloorManagementPanel() {
        this(null, null);
    }

    public FloorManagementPanel(Consumer<Floor> onFloorSelect) {
        this(null, onFloorSelect);
    }

    public FloorManagementPanel(Building building, Consumer<Floor> onFloorSelect) {
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        this.currentBuilding = building;
        this.onFloorSelect = onFloorSelect;

        initUI();
        loadBuildingData(); 
    }
    
    public void setBuilding(Building building) {
        this.currentBuilding = building;
        if (building != null) {
            for (int i = 0; i < cbbBuilding.getItemCount(); i++) {
                Building b = cbbBuilding.getItemAt(i);
                if (b.getId() != null && b.getId().equals(building.getId())) {
                    cbbBuilding.setSelectedIndex(i);
                    break;
                }
            }
        }
        loadFloors();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(UIConstants.BACKGROUND_COLOR);

        JButton btnBack = createBackArrowButton();
        btnBack.addActionListener(e -> {
            MainDashboard main = (MainDashboard) SwingUtilities.getWindowAncestor(this);
            if (main != null) {
                main.showBuildingsPanel(); 
            }
        });
        leftHeader.add(btnBack);

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

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setBackground(UIConstants.BACKGROUND_COLOR);

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

        rightHeader.add(btnBatchAdd);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new OverlayLayout(stackPanel));
        stackPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        overlayPanel = createMaintenanceOverlay();
        overlayPanel.setVisible(false); 
        overlayPanel.setAlignmentX(0.5f);
        overlayPanel.setAlignmentY(0.5f);
        stackPanel.add(overlayPanel);

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
        
        scrollPane.setAlignmentX(0.5f);
        scrollPane.setAlignmentY(0.5f);
        
        stackPanel.add(scrollPane);
        add(stackPanel, BorderLayout.CENTER);
    }
    
    private JButton createBackArrowButton() {
        JButton btn = new JButton(" \u2190 Quay lại"); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(UIConstants.PRIMARY_COLOR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JPanel createMaintenanceOverlay() {
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 120)); 
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        overlay.setOpaque(false);
        overlay.addMouseListener(new MouseAdapter() {}); 

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 25, 25);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-10, getHeight()-10, 25, 25);
                g2.setColor(new Color(255, 112, 67)); 
                RoundRectangle2D topRect = new RoundRectangle2D.Float(0, 0, getWidth()-10, 10, 25, 25);
                g2.setClip(topRect);
                g2.fillRect(0, 0, getWidth()-10, 10);
                g2.setClip(null);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblIcon = new JLabel(new HeaderIcon("MAINTENANCE_ART", 64, new Color(255, 112, 67)));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblTitle = new JLabel("HỆ THỐNG BẢO TRÌ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(66, 66, 66));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblDesc1 = new JLabel("Tòa nhà hiện đang trong trạng thái bảo trì.");
        lblDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc1.setForeground(new Color(100, 100, 100));
        lblDesc1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblDesc2 = new JLabel("Chức năng chỉnh sửa tạm thời bị khóa.");
        lblDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc2.setForeground(new Color(100, 100, 100));
        lblDesc2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        cardPanel.add(lblIcon);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblDesc1);
        cardPanel.add(lblDesc2);
        overlay.add(cardPanel);
        return overlay;
    }

    private void loadBuildingData() {
        List<Building> buildings = buildingDAO.getAllBuildings();
        cbbBuilding.removeAllItems();
        if (buildings.isEmpty()) {
            cbbBuilding.addItem(new Building(null, "Chưa có tòa nhà nào", "", "", "", "Đang hoạt động", false));
        } else {
            for (Building b : buildings) cbbBuilding.addItem(b);
            if (currentBuilding != null) {
                for(int i=0; i<cbbBuilding.getItemCount(); i++) {
                    Building b = cbbBuilding.getItemAt(i);
                    if(b.getId() != null && b.getId().equals(currentBuilding.getId())) { 
                        cbbBuilding.setSelectedIndex(i); 
                        break; 
                    }
                }
            } else cbbBuilding.setSelectedIndex(0);
        }
    }

    // *** METHOD loadFloors() ĐÃ ĐƯỢC SỬA TRIỆT ĐỂ ***
    public void loadFloors() {
        // Hủy worker cũ nếu đang chạy
        if (currentWorker != null && !currentWorker.isDone()) {
            System.out.println("⚠️ [FLOOR] Cancelling previous worker...");
            currentWorker.cancel(true);
        }
        
        cardsContainer.removeAll();
        
        if (currentBuilding == null || currentBuilding.getId() == null) {
            showEmptyMessage("Vui lòng chọn một tòa nhà.");
            cardsContainer.revalidate(); 
            cardsContainer.repaint();
            return;
        }

        System.out.println("🔄 [FLOOR] Loading floors for building: " + currentBuilding.getName() + " (ID: " + currentBuilding.getId() + ")");

        SwingWorker<List<dao.FloorDAO.FloorWithStats>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<dao.FloorDAO.FloorWithStats> doInBackground() {
                if (isCancelled()) return null;
                System.out.println("📊 [FLOOR] Fetching data from database...");
                return floorDAO.getFloorsWithStatsByBuildingId(currentBuilding.getId());
            }

            @Override
            protected void done() {
                // *** KIỂM TRA STALE WORKER TRƯỚC KHI XỬ LÝ ***
                if (this != FloorManagementPanel.this.currentWorker) {
                    System.out.println("⚠️ [FLOOR] Ignored stale worker result");
                    return;
                }

                if (isCancelled()) {
                    System.out.println("❌ [FLOOR] Worker was cancelled");
                    return;
                }
                
                try {
                    List<dao.FloorDAO.FloorWithStats> data = get();
                    System.out.println("✅ [FLOOR] Received " + data.size() + " floors from database");
                    
                    cardsContainer.removeAll();
                    
                    boolean isMaintenance = "Đang bảo trì".equals(currentBuilding.getStatus());
                    updateMaintenanceUI(isMaintenance);

                    if (data.isEmpty()) {
                        showEmptyMessage("Tòa nhà này chưa có tầng nào.");
                    } else {
                        for (dao.FloorDAO.FloorWithStats item : data) {
                            System.out.println("  📍 [FLOOR] Floor " + item.floor.getFloorNumber() + 
                                             " - " + item.floor.getName() + 
                                             ": " + item.stats.rentedApartments + "/" + 
                                             item.stats.totalApartments);
                            
                            FloorCard card = new FloorCard(
                                item.floor, 
                                item.stats, 
                                isMaintenance, 
                                onFloorSelect, 
                                FloorManagementPanel.this::editFloor, 
                                FloorManagementPanel.this::deleteFloor
                            );
                            cardsContainer.add(card);
                        }
                    }
                    
                    cardsContainer.revalidate();
                    cardsContainer.repaint();
                    System.out.println("✅ [FLOOR] UI updated with " + cardsContainer.getComponentCount() + " components");
                    
                } catch (Exception e) {
                    // *** KIỂM TRA STALE WORKER TRONG CATCH ***
                    if (this != FloorManagementPanel.this.currentWorker) {
                        System.out.println("⚠️ [FLOOR] Ignored stale worker exception");
                        return;
                    }
                    
                    System.err.println("❌ [FLOOR] Error loading floors:");
                    e.printStackTrace();
                    cardsContainer.removeAll();
                    showEmptyMessage("Lỗi khi tải dữ liệu tầng.");
                    cardsContainer.revalidate();
                    cardsContainer.repaint();
                }
            }
        };
        
        currentWorker = worker;
        worker.execute();
    }
    
    private void updateMaintenanceUI(boolean isMaintenance) {
        overlayPanel.setVisible(isMaintenance);
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
        JLabel guideLabel = new JLabel(msg); 
        guideLabel.setHorizontalAlignment(SwingConstants.CENTER); 
        guideLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16)); 
        guideLabel.setForeground(Color.GRAY);
        JPanel msgPanel = new JPanel(new BorderLayout()); 
        msgPanel.setBackground(UIConstants.BACKGROUND_COLOR); 
        msgPanel.add(guideLabel, BorderLayout.CENTER);
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
        if (currentBuilding == null || currentBuilding.getId() == null) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tòa nhà trước!"); 
            return; 
        }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BatchAddFloorDialog dialog = new BatchAddFloorDialog(parent, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isSuccess()) loadFloors();
    }

    private void showAddDialog() {
        if (checkMaintenance()) return;
        if (currentBuilding == null || currentBuilding.getId() == null) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tòa nhà trước!"); 
            return; 
        }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
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
        if (checkMaintenance()) return;
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
        if (checkMaintenance()) return;
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa " + floor.getName() + "?", 
            "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (floorDAO.deleteFloor(floor.getId())) {
                loadFloors();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

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
            g2.setColor(getBackground()); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); 
            super.paintComponent(g); 
            g2.dispose(); 
        } 
    }
    
    private static class HeaderIcon implements Icon {
        private String type; 
        private int size; 
        private Color color; 
        
        public HeaderIcon(String type, int size, Color color) { 
            this.type = type; 
            this.size = size; 
            this.color = color; 
        }
        
        @Override 
        public void paintIcon(Component c, Graphics g, int x, int y) { 
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2.setColor(color); 
            g2.setStroke(new BasicStroke(2.0f)); 
            g2.translate(x, y);
            
            if ("PLUS".equals(type)) { 
                g2.drawLine(0, size/2, size, size/2); 
                g2.drawLine(size/2, 0, size/2, size); 
            }
            else if ("LAYER_PLUS".equals(type)) { 
                int w=size-8; int h=size/4; 
                g2.drawRoundRect(4, size/2-4, w, h, 3,3); 
                g2.drawRoundRect(4, size/2+4, w, h, 3,3); 
                g2.drawLine(size/2, 2, size/2, 10); 
                g2.drawLine(size/2-4, 6, size/2+4, 6); 
            }
            else if ("MAINTENANCE_ART".equals(type)) { 
                int cx = size/2; int cy = size/2; int r = size/3;
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawOval(cx-r, cy-r, r*2, r*2);
                g2.setStroke(new BasicStroke(4.0f));
                for(int i=0; i<8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = cx + (int)(Math.cos(angle) * (r-2));
                    int y1 = cy + (int)(Math.sin(angle) * (r-2));
                    int x2 = cx + (int)(Math.cos(angle) * (r+6));
                    int y2 = cy + (int)(Math.sin(angle) * (r+6));
                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.setColor(new Color(90, 90, 90));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(size/4, size-size/4, size-size/4, size/4);
            }
            g2.dispose(); 
        } 
        
        @Override 
        public int getIconWidth() { return size; } 
        
        @Override 
        public int getIconHeight() { return size; }
    }
}