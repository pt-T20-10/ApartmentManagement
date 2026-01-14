package view;

import dao.FloorDAO.FloorStats;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.Consumer;

public class FloorCard extends JPanel { // Tên class phải là FloorCard

    private Floor floor;
    private FloorStats stats;
    private boolean isBuildingMaintenance;
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

        if (isBuildingMaintenance) {
            setToolTipText("🔒 Tòa nhà đang bảo trì - Tạm thời bị khóa");
        } else if ("Đang bảo trì".equals(floor.getStatus())) {
            setToolTipText("⚠️ Tầng này đang bảo trì");
        }
    }

    private void initCardUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblName = new JLabel(floor.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(new Color(33, 33, 33));
        topPanel.add(lblName, BorderLayout.WEST);

        // STATUS BADGE
        String status = floor.getStatus();
        if (status != null && !"Đang hoạt động".equals(status)) {
            StatusBadge statusBadge = new StatusBadge(status);
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(statusBadge);
            topPanel.add(badgePanel, BorderLayout.EAST);
        }
        
        add(topPanel, BorderLayout.NORTH);

        // INFO
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

        OccupancyBar progressBar = new OccupancyBar(stats.rentedApartments, stats.totalApartments);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(progressBar);
        
        add(centerContent, BorderLayout.CENTER);

        // ACTIONS
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JButton btnEdit = createIconButton("EDIT", new Color(100, 149, 237));
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
        g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.setColor(new Color(220, 220, 220)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        if (isBuildingMaintenance) { g2.setColor(new Color(245, 245, 245, 180)); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); }
        g2.dispose(); super.paintComponent(g);
    }

    private JButton createIconButton(String iconType, Color color) {
        Color iconColor = isBuildingMaintenance ? Color.LIGHT_GRAY : color;
        JButton btn = new JButton(new CardIcon(iconType, 16, iconColor));
        btn.setPreferredSize(new Dimension(32, 32)); btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 0, 0, 0) {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                if (isBuildingMaintenance) return;
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 230, 230)); g2.drawRoundRect(x, y, width - 1, height - 1, 8, 8); g2.dispose();
            }
        });
        btn.setBorderPainted(true); return btn;
    }

    // --- Inner Classes ---
    private static class StatusBadge extends JLabel {
        private Color bgColor, textColor;
        public StatusBadge(String status) {
            super(status); setFont(new Font("Segoe UI", Font.BOLD, 12)); setBorder(new EmptyBorder(5, 12, 5, 12));
            if ("Đang hoạt động".equals(status)) { bgColor = new Color(232, 245, 233); textColor = new Color(46, 125, 50); } 
            else if ("Đang bảo trì".equals(status)) { bgColor = new Color(255, 243, 224); textColor = new Color(239, 108, 0); } 
            else { bgColor = new Color(255, 235, 238); textColor = new Color(198, 40, 40); }
            setForeground(textColor);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); g2.dispose(); super.paintComponent(g);
        }
    }

    private static class OccupancyBar extends JPanel {
        int rented, total; public OccupancyBar(int rented, int total) { this.rented = rented; this.total = total; setPreferredSize(new Dimension(200, 25)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 8; int percent = (total == 0) ? 0 : (rented * 100 / total); int blueW = (int)(w * (percent / 100.0));
            g2.setColor(new Color(232, 245, 233)); g2.fillRoundRect(0, 0, w, h, arc, arc);
            if (blueW > 0) { g2.setColor(new Color(67, 160, 71)); if (percent == 100) g2.fillRoundRect(0, 0, w, h, arc, arc); else { g2.fillRoundRect(0, 0, blueW, h, arc, arc); g2.fillRect(blueW - arc, 0, arc, h); } }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); FontMetrics fm = g2.getFontMetrics();
            if (blueW > 50) { String txt = rented + " Đang thuê"; g2.setColor(Color.WHITE); g2.drawString(txt, 10, (h + fm.getAscent()) / 2 - 2); }
            if (w - blueW > 40) { String txt = (total - rented) + " Trống"; g2.setColor(new Color(27, 94, 32)); g2.drawString(txt, w - fm.stringWidth(txt) - 10, (h + fm.getAscent()) / 2 - 2); } g2.dispose();
        }
    }

    private static class CardIcon implements Icon {
        private String type; private int size; private Color color; public CardIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.translate(x, y);
            if ("EDIT".equals(type)) { g2.rotate(Math.toRadians(45), size/2.0, size/2.0); g2.drawRoundRect(size/2-2, 0, 4, size-4, 1, 1); g2.drawLine(size/2-2, 3, size/2+2, 3); java.awt.geom.Path2D tip = new java.awt.geom.Path2D.Float(); tip.moveTo(size/2-2, size-4); tip.lineTo(size/2, size); tip.lineTo(size/2+2, size-4); g2.fill(tip); } 
            else if ("DELETE".equals(type)) { int w = size-6; int h = size-4; int mx = 3; int my = 4; g2.drawRoundRect(mx, my, w, h, 3, 3); g2.drawLine(1, my, size-1, my); g2.drawArc(size/2-2, 0, 4, 4, 0, 180); g2.drawLine(size/2-2, my+3, size/2-2, my+h-3); g2.drawLine(size/2+2, my+3, size/2+2, my+h-3); }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}