package view;

import model.Apartment;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class ApartmentCard extends JPanel {

    private Apartment apartment;
    private Consumer<Apartment> onEdit;
    private Consumer<Apartment> onDelete;
    private static final DecimalFormat df = new DecimalFormat("#,###");

    public ApartmentCard(Apartment apartment, Consumer<Apartment> onEdit, Consumer<Apartment> onDelete) {
        this.apartment = apartment;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        setPreferredSize(new Dimension(300, 180)); 
        
        // --- [MỚI] SỰ KIỆN CLICK TOÀN CARD ĐỂ SỬA ---
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onEdit != null) onEdit.accept(apartment);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(252, 252, 252));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(null);
                repaint();
            }
        });

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 18, 12, 18));

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        
        JLabel lblRoom = new JLabel("P. " + apartment.getRoomNumber());
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblRoom.setForeground(new Color(33, 33, 33));
        
        String type = apartment.getApartmentType() != null ? apartment.getApartmentType() : "Standard";
        JLabel lblType = new JLabel(type);
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblType.setForeground(Color.GRAY);
        lblType.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(1, 5, 1, 5)
        ));

        titleGroup.add(lblRoom);
        titleGroup.add(lblType);
        headerPanel.add(titleGroup, BorderLayout.WEST);

        headerPanel.add(createStatusBadge(apartment.getStatus()), BorderLayout.EAST);

        // === 2. BODY ===
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(12, 0, 10, 0));

        JPanel specRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        specRow.setOpaque(false);
        specRow.add(createIconLabel("AREA", apartment.getArea() + " m²"));
        specRow.add(Box.createHorizontalStrut(12));
        specRow.add(createIconLabel("BED", apartment.getBedroomCount() + " PN"));
        specRow.add(Box.createHorizontalStrut(12));
        specRow.add(createIconLabel("BATH", apartment.getBathroomCount() + " PT"));

        String priceTxt = (apartment.getBasePrice() != null) ? df.format(apartment.getBasePrice()) + " đ" : "Liên hệ";
        JLabel lblPrice = new JLabel(priceTxt);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPrice.setForeground(UIConstants.PRIMARY_COLOR);
        lblPrice.setBorder(new EmptyBorder(8, 0, 0, 0));

        bodyPanel.add(specRow);
        bodyPanel.add(lblPrice);

        // === 3. FOOTER ===
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        
        String desc = apartment.getDescription();
        if (desc == null || desc.isEmpty()) desc = "Không có ghi chú";
        if (desc.length() > 20) desc = desc.substring(0, 18) + "...";
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDesc.setForeground(new Color(150, 150, 150));
        footerPanel.add(lblDesc, BorderLayout.WEST);

        // --- [CẬP NHẬT] NÚT BẤM VỚI ICON GIỐNG FLOOR CARD ---
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);
        
        JButton btnEdit = createIconButton("EDIT", new Color(117, 117, 117));
        btnEdit.addActionListener(e -> onEdit.accept(apartment));
        // Chặn sự kiện lan ra Card khi nhấn nút
        btnEdit.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { e.consume(); } });
        
        JButton btnDelete = createIconButton("DELETE", new Color(229, 57, 53));
        btnDelete.addActionListener(e -> onDelete.accept(apartment));
        btnDelete.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { e.consume(); } });

        btnGroup.add(btnEdit);
        btnGroup.add(btnDelete);
        footerPanel.add(btnGroup, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private StatusBadge createStatusBadge(String status) {
        if ("RENTED".equalsIgnoreCase(status) || "Đã thuê".equalsIgnoreCase(status)) {
            return new StatusBadge("Đã thuê", new Color(232, 245, 233), new Color(46, 125, 50));
        } else if ("MAINTENANCE".equalsIgnoreCase(status) || "Bảo trì".equalsIgnoreCase(status)) {
            return new StatusBadge("Bảo trì", new Color(255, 243, 224), new Color(239, 108, 0));
        } else {
            return new StatusBadge("Trống", new Color(237, 247, 255), new Color(25, 118, 210));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
        g2.setColor(new Color(230, 230, 230));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
        g2.dispose();
    }

    private JLabel createIconLabel(String icon, String text) {
        JLabel l = new JLabel(" " + text);
        l.setIcon(new CardIcon(icon, 14, new Color(100, 100, 100)));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(70, 70, 70));
        return l;
    }

    private JButton createIconButton(String iconType, Color color) {
        JButton btn = new JButton(new CardIcon(iconType, 18, color));
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- Badge Trạng thái ---
    private static class StatusBadge extends JLabel {
        private Color bgColor, textColor;
        public StatusBadge(String text, Color bg, Color txt) {
            super(text);
            this.bgColor = bg; this.textColor = txt;
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setForeground(textColor);
            setBorder(new EmptyBorder(3, 10, 3, 10));
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

    // --- [SỬA LẠI] VẼ ICON THỦ CÔNG GIỐNG FLOOR CARD ---
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
                tip.moveTo(size/2-2, size-4); tip.lineTo(size/2, size); tip.lineTo(size/2+2, size-4);
                g2.fill(tip);
            } else if ("DELETE".equals(type)) {
                int w = size-6; int h = size-4; int mx = 3; int my = 4;
                g2.drawRoundRect(mx, my, w, h, 3, 3);
                g2.drawLine(1, my, size-1, my);
                g2.drawArc(size/2-2, 0, 4, 4, 0, 180);
                g2.drawLine(size/2-2, my+3, size/2-2, my+h-3);
                g2.drawLine(size/2+2, my+3, size/2+2, my+h-3);
            } else if ("AREA".equals(type)) {
                g2.drawRect(2, 2, size-4, size-4);
                g2.drawLine(size/2, 2, size/2, size-2);
                g2.drawLine(2, size/2, size-2, size/2);
            } else if ("BED".equals(type)) {
                g2.drawRoundRect(1, 4, size-2, size-8, 2, 2);
                g2.drawLine(4, 4, 4, 2); g2.drawLine(size-4, 4, size-4, 2);
            } else if ("BATH".equals(type)) {
                g2.drawArc(1, 2, size-2, size, 0, 180);
                g2.drawRect(2, size/2, size-4, 4);
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}