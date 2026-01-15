package view;

import dao.FloorDAO.FloorStats;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.Consumer;

public class FloorCard extends JPanel {

    private Floor floor;
    private FloorStats stats;
    private boolean isBuildingMaintenance;
    
    // Callback sự kiện
    private Consumer<Floor> onEdit;
    private Consumer<Floor> onDelete;

    public FloorCard(Floor floor, FloorStats stats, boolean isBuildingMaintenance, 
                     Consumer<Floor> onEdit, Consumer<Floor> onDelete) {
        this.floor = floor;
        this.stats = stats;
        this.isBuildingMaintenance = isBuildingMaintenance;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        setPreferredSize(new Dimension(300, 160));
        initCardUI();

        // Tooltip thông minh
        if (isBuildingMaintenance) {
            setToolTipText("🔒 Tòa nhà đang bảo trì - Tạm thời bị khóa");
        } else if ("Bảo trì".equals(floor.getStatus())) {
            setToolTipText("⚠️ Tầng này đang bảo trì");
        }
    }

    private void initCardUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // === 1. HEADER (Tên + Trạng thái) ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Tên Tầng
        JLabel lblName = new JLabel(floor.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(new Color(33, 33, 33));
        topPanel.add(lblName, BorderLayout.WEST);

        // --- BADGE TRẠNG THÁI ---
        String status = floor.getStatus();
        if ("Bảo trì".equals(status)) {
            StatusBadge badge = new StatusBadge("Bảo trì", new Color(255, 248, 225), new Color(255, 143, 0));
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(badge);
            topPanel.add(badgePanel, BorderLayout.EAST);
        }
        
        add(topPanel, BorderLayout.NORTH);

        // === 2. INFO (Số liệu) ===
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        centerContent.setBorder(new EmptyBorder(10, 0, 0, 0)); 

        JLabel lblTotal = new JLabel("Tổng số căn: " + stats.totalApartments);
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotal.setForeground(Color.GRAY);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerContent.add(lblTotal);
        centerContent.add(Box.createVerticalStrut(10)); 

        // Thanh tiến độ
        OccupancyBar progressBar = new OccupancyBar(stats.rentedApartments, stats.totalApartments);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(progressBar);
        
        add(centerContent, BorderLayout.CENTER);

        // === 3. ACTIONS (Nút bấm) ===
        // SỬA: Đưa các nút về phía phải, bỏ viền
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Nút Sửa: Màu Xám (Giống BuildingCard)
        JButton btnEdit = createIconButton("EDIT", new Color(117, 117, 117));
        
        // Nút Xóa: Màu Đỏ (Giữ nguyên)
        JButton btnDelete = createIconButton("DELETE", new Color(239, 83, 80));

        if (isBuildingMaintenance) {
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        } else {
            btnEdit.addActionListener(e -> { if (onEdit != null) onEdit.accept(floor); });
            btnDelete.addActionListener(e -> { if (onDelete != null) onDelete.accept(floor); });
        }

        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        
        add(actionPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền Card
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Hiệu ứng mờ khi bảo trì
        if (isBuildingMaintenance) {
            g2.setColor(new Color(245, 245, 245, 180));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    // --- CẬP NHẬT HÀM TẠO NÚT (Bỏ viền, Tăng size) ---
    private JButton createIconButton(String iconType, Color color) {
        Color iconColor = isBuildingMaintenance ? Color.LIGHT_GRAY : color;
        // Tăng size icon lên 20 (giống BuildingCard)
        JButton btn = new JButton(new CardIcon(iconType, 20, iconColor));
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Đã xóa phần setBorder vẽ viền tròn
        return btn;
    }

    // --- INNER CLASS: StatusBadge ---
    private static class StatusBadge extends JLabel {
        private Color bgColor, textColor;
        public StatusBadge(String text, Color bg, Color txt) {
            super(text); 
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setBorder(new EmptyBorder(2, 8, 2, 8));
            this.bgColor = bg;
            this.textColor = txt;
            setForeground(textColor);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose(); 
            super.paintComponent(g);
        }
    }

    // --- INNER CLASS: Progress Bar ---
    private static class OccupancyBar extends JPanel {
        int rented, total;
        public OccupancyBar(int rented, int total) { this.rented = rented; this.total = total; setPreferredSize(new Dimension(200, 25)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 8; int percent = (total == 0) ? 0 : (rented * 100 / total); int blueW = (int)(w * (percent / 100.0));
            g2.setColor(new Color(232, 245, 233)); g2.fillRoundRect(0, 0, w, h, arc, arc);
            if (blueW > 0) { 
                g2.setColor(new Color(67, 160, 71)); 
                if (percent == 100) g2.fillRoundRect(0, 0, w, h, arc, arc); else { g2.fillRoundRect(0, 0, blueW, h, arc, arc); g2.fillRect(blueW - arc, 0, arc, h); } 
            }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); FontMetrics fm = g2.getFontMetrics();
            if (blueW > 50) { String txt = rented + " Đang thuê"; g2.setColor(Color.WHITE); g2.drawString(txt, 10, (h + fm.getAscent()) / 2 - 2); }
            if (w - blueW > 40) { String txt = (total - rented) + " Trống"; g2.setColor(new Color(27, 94, 32)); g2.drawString(txt, w - fm.stringWidth(txt) - 10, (h + fm.getAscent()) / 2 - 2); } g2.dispose();
        }
    }

    // --- INNER CLASS: Icon (Path2D chuẩn) ---
    private static class CardIcon implements Icon {
        private String type; private int size; private Color color;
        public CardIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.translate(x, y);
            if ("EDIT".equals(type)) { 
                g2.rotate(Math.toRadians(45), size/2.0, size/2.0); g2.drawRoundRect(size/2-2, 0, 4, size-4, 1, 1); g2.drawLine(size/2-2, 3, size/2+2, 3); 
                java.awt.geom.Path2D tip = new java.awt.geom.Path2D.Float(); tip.moveTo(size/2-2, size-4); tip.lineTo(size/2, size); tip.lineTo(size/2+2, size-4); g2.fill(tip); 
            } else if ("DELETE".equals(type)) { 
                int w = size-6; int h = size-4; int mx = 3; int my = 4; g2.drawRoundRect(mx, my, w, h, 3, 3); g2.drawLine(1, my, size-1, my); g2.drawArc(size/2-2, 0, 4, 4, 0, 180); g2.drawLine(size/2-2, my+3, size/2-2, my+h-3); g2.drawLine(size/2+2, my+3, size/2+2, my+h-3); 
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}