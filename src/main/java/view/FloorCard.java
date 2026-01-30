package view;

import dao.FloorDAO.FloorStats;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.function.Consumer;

public class FloorCard extends JPanel {

    private Floor floor;
    private FloorStats stats;
    private boolean isBuildingMaintenance;
    
    // Callback sự kiện
    private Consumer<Floor> onSelect; 
    private Consumer<Floor> onEdit;
    private Consumer<Floor> onDelete;

    public FloorCard(Floor floor, FloorStats stats, boolean isBuildingMaintenance, 
                     Consumer<Floor> onSelect, Consumer<Floor> onEdit, Consumer<Floor> onDelete) {
        this.floor = floor;
        this.stats = stats;
        this.isBuildingMaintenance = isBuildingMaintenance;
        this.onSelect = onSelect;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        setPreferredSize(new Dimension(300, 160));
        
        // Thiết lập sự kiện click
        if (isBuildingMaintenance) {
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        } else {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isBuildingMaintenance) {
                    return; 
                }
                
                if (onSelect != null) {
                    onSelect.accept(floor);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isBuildingMaintenance) {
                    setBackground(new Color(252, 252, 252));
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(null);
                repaint();
            }
        });

        initCardUI();

        // Tooltip
        if (isBuildingMaintenance) {
            setToolTipText("🔒 Tòa nhà đang bảo trì - Tạm thời bị khóa");
        } else if (isMaintenance(floor.getStatus())) {
            setToolTipText("⚠️ Tầng đang bảo trì - Click để quản lý căn hộ");
        }
    }

    private boolean isMaintenance(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("bảo trì") || s.contains("maintenance") || s.contains("sửa chữa");
    }

    private void initCardUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // === 1. HEADER ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblName = new JLabel(floor.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        if (isMaintenance(floor.getStatus())) {
            lblName.setForeground(Color.GRAY);
        } else {
            lblName.setForeground(new Color(33, 33, 33));
        }
        topPanel.add(lblName, BorderLayout.WEST);

        // Badge
        String statusText = floor.getStatus();
        if (statusText == null || statusText.isEmpty()) statusText = "Hoạt động";

        StatusBadge badge;
        if (isMaintenance(statusText)) {
            badge = new StatusBadge("Bảo trì", new Color(255, 243, 224), new Color(239, 108, 0));
        } else {
            badge = new StatusBadge("Hoạt động", new Color(232, 245, 233), new Color(46, 125, 50));
        }

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(badge);
        topPanel.add(badgePanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // === 2. INFO ===
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        centerContent.setBorder(new EmptyBorder(10, 0, 0, 0)); 

        // ✅ FIX: Hiển thị tổng số căn hộ
        JLabel lblTotal = new JLabel("Tổng số căn: " + stats.totalApartments);
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotal.setForeground(Color.GRAY);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerContent.add(lblTotal);
        centerContent.add(Box.createVerticalStrut(10)); 

        // ✅ FIX: Progress bar tính cả RENTED + OWNED
        int occupiedApartments = stats.rentedApartments + stats.ownedApartments;
        OccupancyBar progressBar = new OccupancyBar(
            occupiedApartments, 
            stats.totalApartments,
            stats.rentedApartments,
            stats.ownedApartments
        );
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(progressBar);
        
        add(centerContent, BorderLayout.CENTER);

        // === 3. ACTIONS ===
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JButton btnEdit = createIconButton("EDIT", new Color(117, 117, 117));
        JButton btnDelete = createIconButton("DELETE", new Color(239, 83, 80));

        if (isBuildingMaintenance) {
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        } else {
            btnEdit.addActionListener(e -> { if (onEdit != null) onEdit.accept(floor); });
            btnDelete.addActionListener(e -> { if (onDelete != null) onDelete.accept(floor); });
            
            btnEdit.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { e.consume(); } });
            btnDelete.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { e.consume(); } });
        }

        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        
        add(actionPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        if (isBuildingMaintenance) {
            g2.setColor(new Color(245, 245, 245, 180));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        else if (isMaintenance(floor.getStatus())) {
            g2.setColor(new Color(255, 243, 224, 60)); 
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    private JButton createIconButton(String iconType, Color color) {
        Color iconColor = isBuildingMaintenance ? Color.LIGHT_GRAY : color;
        JButton btn = new JButton(new CardIcon(iconType, 20, iconColor));
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- INNER CLASSES ---

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

    /**
     * ✅ UPDATED: OccupancyBar hiển thị cả RENTED + OWNED
     */
    private static class OccupancyBar extends JPanel {
        int occupied, total, rented, owned;

        public OccupancyBar(int occupied, int total, int rented, int owned) {
            this.occupied = occupied; // RENTED + OWNED
            this.total = total;
            this.rented = rented;
            this.owned = owned;
            setPreferredSize(new Dimension(200, 26)); 
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10; 
            
            double percent = (total == 0) ? 0 : (double) occupied / total;
            int filledWidth = (int) (w * percent);

            // Background (còn trống)
            g2.setColor(new Color(240, 240, 240));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // ✅ Filled area - tính cả RENTED + OWNED
            if (filledWidth > 0) {
                // Gradient hoặc solid color
                g2.setColor(new Color(33, 150, 243)); // Blue
                
                if (occupied == total && total > 0) {
                    g2.fillRoundRect(0, 0, w, h, arc, arc); 
                } else {
                    g2.fillRoundRect(0, 0, filledWidth, h, arc, arc);
                    if (filledWidth > arc) {
                        g2.fillRect(filledWidth - arc, 0, arc, h);
                    }
                }
            }

            // ✅ Text hiển thị chi tiết
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            
            // Hiển thị: "X/Y đã sử dụng" hoặc chi tiết hơn
            String statusText;
            if (owned > 0 && rented > 0) {
                // Có cả thuê và mua
                statusText = String.format("%d/%d (Thuê: %d | Bán: %d)", 
                    occupied, total, rented, owned);
            } else if (owned > 0) {
                // Chỉ có bán
                statusText = String.format("%d/%d đã bán", owned, total);
            } else {
                // Chỉ có thuê hoặc không có gì
                statusText = String.format("%d/%d đã thuê", rented, total);
            }
            
            int textX = (w - fm.stringWidth(statusText)) / 2;
            int textY = (h + fm.getAscent()) / 2 - 2;

            // Màu text: trắng nếu > 50% filled, xám nếu < 50%
            g2.setColor(percent > 0.5 ? Color.WHITE : new Color(100, 100, 100));
            g2.drawString(statusText, textX, textY);

            g2.dispose();
        }
    }

    private static class CardIcon implements Icon {
        private String type; private int size; private Color color;
        public CardIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); 
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
            g2.translate(x, y);
            
            if ("EDIT".equals(type)) { 
                g2.rotate(Math.toRadians(45), size/2.0, size/2.0); 
                g2.drawRoundRect(size/2-2, 0, 4, size-4, 1, 1); 
                g2.drawLine(size/2-2, 3, size/2+2, 3); 
                Path2D tip = new Path2D.Float(); 
                tip.moveTo(size/2-2, size-4); 
                tip.lineTo(size/2, size); 
                tip.lineTo(size/2+2, size-4); 
                g2.fill(tip); 
            } else if ("DELETE".equals(type)) { 
                int w = size-6; int h = size-4; int mx = 3; int my = 4; 
                g2.drawRoundRect(mx, my, w, h, 3, 3); 
                g2.drawLine(1, my, size-1, my); 
                g2.drawArc(size/2-2, 0, 4, 4, 0, 180); 
                g2.drawLine(size/2-2, my+3, size/2-2, my+h-3); 
                g2.drawLine(size/2+2, my+3, size/2+2, my+h-3); 
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; } 
        @Override public int getIconHeight() { return size; }
    }
}