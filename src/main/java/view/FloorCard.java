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
    private Consumer<Floor> onSelect; // Hành động khi nhấn vào Card để xem căn hộ
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
        
        // --- THIẾT LẬP SỰ KIỆN CLICK CHO TOÀN BỘ CARD ---
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Chuyển sang trang căn hộ khi nhấn vào vùng trống của Card
                if (onSelect != null) {
                    onSelect.accept(floor);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Hiệu ứng hover: Đổi màu nền nhẹ (trừ khi đang bị mờ do bảo trì tòa nhà)
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

        // Tooltip thông minh
        if (isBuildingMaintenance) {
            setToolTipText("🔒 Tòa nhà đang bảo trì - Tạm thời bị khóa");
        } else if (isMaintenance(floor.getStatus())) {
            setToolTipText("⚠️ Tầng này đang bảo trì");
        }
    }

    // Hàm kiểm tra trạng thái thông minh
    private boolean isMaintenance(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("bảo trì") || s.contains("maintenance") || s.contains("sửa chữa");
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

        // BADGE TRẠNG THÁI
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

        OccupancyBar progressBar = new OccupancyBar(stats.rentedApartments, stats.totalApartments);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(progressBar);
        
        add(centerContent, BorderLayout.CENTER);

        // === 3. ACTIONS (Nút bấm) ===
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
            
            // NGĂN CHẶN CLICK LAN TỎA: Khi nhấn nút thì Card không nhận được sự kiện click
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

        // Vẽ nền Card (Hỗ trợ đổi màu khi hover)
        g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        
        // Vẽ viền
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Hiệu ứng mờ khi bảo trì Tòa nhà
        if (isBuildingMaintenance) {
            g2.setColor(new Color(245, 245, 245, 180));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        // Hiệu ứng mờ nhẹ khi bảo trì Tầng
        else if (isMaintenance(floor.getStatus())) {
            g2.setColor(new Color(255, 253, 231, 50)); 
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

    private static class OccupancyBar extends JPanel {
        int rented, total;

        public OccupancyBar(int rented, int total) {
            this.rented = rented;
            this.total = total;
            setPreferredSize(new Dimension(200, 26)); // Chiều cao tiêu chuẩn
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10; // Độ bo góc thanh tiến độ
            
            // Tính toán phần trăm
            double percent = (total == 0) ? 0 : (double) rented / total;
            int filledWidth = (int) (w * percent);

            // 1. Vẽ nền thanh (Màu xám nhạt/xanh nhạt)
            g2.setColor(new Color(240, 240, 240));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // 2. Vẽ phần đã thuê (Màu xanh thương hiệu)
            if (filledWidth > 0) {
                // Sử dụng màu xanh giống UIConstants.PRIMARY_COLOR hoặc màu xanh lá
                g2.setColor(new Color(33, 150, 243)); 
                
                if (rented == total && total > 0) {
                    g2.fillRoundRect(0, 0, w, h, arc, arc); // Đầy 100%
                } else {
                    // Vẽ phần bo góc bên trái và phẳng bên phải để nối tiếp
                    g2.fillRoundRect(0, 0, filledWidth, h, arc, arc);
                    if (filledWidth > arc) {
                        g2.fillRect(filledWidth - arc, 0, arc, h);
                    }
                }
            }

            // 3. Vẽ văn bản hiển thị thông tin
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String statusText = rented + "/" + total + " Căn hộ đã thuê";
            int textX = (w - fm.stringWidth(statusText)) / 2;
            int textY = (h + fm.getAscent()) / 2 - 2;

            // Đổi màu chữ thông minh dựa trên độ lấp đầy
            g2.setColor(percent > 0.5 ? Color.WHITE : new Color(100, 100, 100));
            g2.drawString(statusText, textX, textY);

            g2.dispose();
        }
    }

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