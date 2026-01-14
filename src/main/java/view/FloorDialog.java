package view;

import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Path2D;

public class FloorDialog extends JDialog {

    private JTextField txtFloorNumber; // Nhập số tầng (1, 2, 3...)
    private JTextField txtName;        // Nhập tên hiển thị (Tầng 1, Tầng G...)
    private JTextArea txtDesc;         // Mô tả
    
    private boolean confirmed = false;
    private Floor floor;

    public FloorDialog(Frame owner, Floor floor) {
        super(owner, floor.getId() == null ? "Thêm Tầng Mới" : "Cập Nhật Tầng", true);
        this.floor = floor;
        
        initUI();
        fillData();
        
        setSize(450, 500); // Kích thước gọn gàng
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        headerPanel.setBackground(Color.WHITE);
        
        // Icon Tầng (3 lớp chồng lên nhau)
        JLabel iconLabel = new JLabel(new SimpleIcon("FLOOR_STACK", 45, UIConstants.PRIMARY_COLOR));
        
        JLabel titleLabel = new JLabel(floor.getId() == null ? "THÊM TẦNG MỚI" : "CẬP NHẬT TẦNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === 2. FORM ===
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 50, 20, 50)); // Lề 50px

        txtFloorNumber = createRoundedField();
        txtName = createRoundedField();
        
        txtDesc = new JTextArea(5, 20);
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(150, 150, 150), 1));
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(createLabel("Số Tầng (VD: 1, 2, 0...) (*)"));
        formPanel.add(txtFloorNumber);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createLabel("Tên Hiển Thị (VD: Tầng 1, Tầng Trệt) (*)"));
        formPanel.add(txtName);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createLabel("Mô Tả / Ghi Chú"));
        formPanel.add(scrollDesc);
        
        add(formPanel, BorderLayout.CENTER);

        // === 3. BUTTONS ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton btnCancel = new RoundedButton("Hủy Bỏ", 10);
        btnCancel.setBackground(new Color(225, 225, 225));
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new RoundedButton("Lưu Lại", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setIcon(new SimpleIcon("CHECK", 14, Color.WHITE));
        btnSave.setPreferredSize(new Dimension(120, 38));
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillData() {
        if (floor.getId() != null) {
            txtFloorNumber.setText(String.valueOf(floor.getFloorNumber()));
            txtName.setText(floor.getName());
            txtDesc.setText(floor.getDescription()); // Giả sử model Floor đã có field description
        }
    }

    private void onSave() {
        String numStr = txtFloorNumber.getText().trim();
        String name = txtName.getText().trim();

        if (numStr.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Số tầng và Tên tầng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int num = Integer.parseInt(numStr);
            floor.setFloorNumber(num);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tầng phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        floor.setName(name);
        // floor.setDescription(txtDesc.getText().trim()); // Bỏ comment nếu model có description
        
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Floor getFloor() { return floor; }

    // --- Helpers UI ---
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(60, 60, 60));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }
    
    private JTextField createRoundedField() {
        JTextField f = new RoundedTextField(10);
        f.setFont(UIConstants.FONT_REGULAR);
        f.setPreferredSize(new Dimension(100, 35));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    // --- Inner Classes (Styles) ---
    private static class RoundedTextField extends JTextField {
        private int arc;
        public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 12, 5, 12)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc);
            g2.setColor(new Color(150, 150, 150)); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
            super.paintComponent(g); g2.dispose();
        }
    }
    private static class RoundedButton extends JButton {
        private int arc;
        public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isArmed()) g2.setColor(getBackground().darker()); else g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose();
        }
    }
    private static class SimpleIcon implements Icon {
        private String type; private int size; private Color color;
        public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.translate(x, y);
            if ("FLOOR_STACK".equals(type)) { // Icon 3 lớp chồng lên nhau
                int h = size/4; int w = size*2/3; int cx = (size-w)/2;
                g2.fillRoundRect(cx, size-h, w, h, 4, 4); // Tầng 1
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                g2.fillRoundRect(cx, size-h*2+4, w, h, 4, 4); // Tầng 2
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
                g2.fillRoundRect(cx, size-h*3+8, w, h, 4, 4); // Tầng 3
            } else if ("CHECK".equals(type)) {
                g2.setStroke(new BasicStroke(2.5f)); Path2D p = new Path2D.Float();
                p.moveTo(2, size/2); p.lineTo(size/2 - 2, size - 3); p.lineTo(size - 2, 3); g2.draw(p);
            } g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}