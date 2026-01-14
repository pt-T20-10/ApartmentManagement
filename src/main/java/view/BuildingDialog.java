package view;

import model.Building;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Path2D;

public class BuildingDialog extends JDialog {

    private JTextField txtName;
    private JTextField txtAddress;
    private JTextField txtManager;
    private JTextArea txtDesc;
    
    private boolean confirmed = false;
    private Building building;

    public BuildingDialog(Frame owner, Building building) {
        super(owner, building == null ? "Thêm Tòa Nhà Mới" : "Cập Nhật Thông Tin", true);
        this.building = (building == null) ? new Building() : building;
        
        initUI();
        fillData();
        
        setSize(500, 680); // Tăng chiều cao xíu cho thoải mái
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(new SimpleIcon("BUILDING_BIG", 45, UIConstants.PRIMARY_COLOR));
        JLabel titleLabel = new JLabel(building.getId() == null ? "THÊM TÒA NHÀ MỚI" : "CẬP NHẬT TÒA NHÀ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === 2. FORM NHẬP LIỆU (CHIA 2 PHẦN) ===
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 40, 20, 40)); // Lề chung: 40px

        // --- PHẦN A: THÔNG TIN CHÍNH (Thụt vào trong) ---
        JPanel topGroup = new JPanel();
        topGroup.setLayout(new BoxLayout(topGroup, BoxLayout.Y_AXIS));
        topGroup.setBackground(Color.WHITE);
        // QUAN TRỌNG: Thêm lề 30px mỗi bên để các ô này "thụt vào" so với mô tả
        topGroup.setBorder(new EmptyBorder(0, 30, 0, 30)); 

        txtName = createRoundedField();
        txtAddress = createRoundedField();
        txtManager = createRoundedField();

        topGroup.add(createLabel("Tên Tòa Nhà (*)"));
        topGroup.add(txtName);
        topGroup.add(Box.createVerticalStrut(12));
        
        topGroup.add(createLabel("Địa Chỉ (*)"));
        topGroup.add(txtAddress);
        topGroup.add(Box.createVerticalStrut(12));

        topGroup.add(createLabel("Người Quản Lý"));
        topGroup.add(txtManager);
        
        formPanel.add(topGroup);
        formPanel.add(Box.createVerticalStrut(20)); // Khoảng cách giữa 2 phần

        // --- PHẦN B: MÔ TẢ (Rộng hơn - Không thụt lề thêm) ---
        JPanel descGroup = new JPanel();
        descGroup.setLayout(new BoxLayout(descGroup, BoxLayout.Y_AXIS));
        descGroup.setBackground(Color.WHITE);
        // Không set EmptyBorder thêm -> Nó sẽ rộng hơn phần trên
        
        txtDesc = new JTextArea(8, 20); // 8 dòng -> Cao hơn
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(150, 150, 150), 1)); 
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        descGroup.add(createLabel("Mô Tả / Ghi Chú (Chi tiết)"));
        descGroup.add(scrollDesc);
        
        formPanel.add(descGroup);
        
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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 5, 0)); 
        return label;
    }

    private JTextField createRoundedField() {
        JTextField field = new RoundedTextField(10); 
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(100, 38)); 
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void fillData() {
        if (building.getId() != null) {
            txtName.setText(building.getName());
            txtAddress.setText(building.getAddress());
            txtManager.setText(building.getManagerName());
            txtDesc.setText(building.getDescription());
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String addr = txtAddress.getText().trim();

        if (name.isEmpty() || addr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên và Địa chỉ!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        building.setName(name);
        building.setAddress(addr);
        building.setManagerName(txtManager.getText().trim());
        building.setDescription(txtDesc.getText().trim());
        if (building.getId() == null) building.setDeleted(false); 

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Building getBuilding() { return building; }

    // --- CÁC CLASS CON (GIỮ NGUYÊN) ---
    private static class RoundedTextField extends JTextField {
        private int arc;
        public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 12, 5, 12)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc);
            g2.setColor(new Color(150, 150, 150)); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
            super.paintComponent(g); g2.dispose();
        }
    }

    private static class RoundedButton extends JButton {
        private int arc;
        public RoundedButton(String text, int arc) {
            super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isArmed()) g2.setColor(getBackground().darker()); else g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose();
        }
    }

    private static class SimpleIcon implements Icon {
        private String type; private int size; private Color color;
        public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.setStroke(new BasicStroke(1.5f)); g2.translate(x, y);
            if ("BUILDING_BIG".equals(type)) { 
                int baseY = size - 4; int midW = size * 36 / 100; int midH = size * 80 / 100; int midX = (size - midW) / 2; g2.fillRect(midX, baseY - midH, midW, midH);
                int leftW = size * 15 / 100; int leftH = size * 40 / 100; int leftX = midX - leftW - 2; g2.drawRect(leftX, baseY - leftH, leftW, leftH); g2.fillRect(leftX + leftW, baseY - leftH + 5, 2, leftH - 5);
                int rightW = size * 18 / 100; int rightH = size * 60 / 100; int rightX = midX + midW + 2; g2.drawRect(rightX, baseY - rightH, rightW, rightH);
                g2.setColor(Color.WHITE); int winSize = size/12; int gap = size/16; int startWX = midX + (midW - (3*winSize + 2*gap))/2; int startWY = baseY - midH + size/6;
                for(int r=0; r<4; r++) for(int c1=0; c1<3; c1++) g2.fillRect(startWX + c1*(winSize+gap), startWY + r*(winSize+gap), winSize, winSize);
            } else if ("CHECK".equals(type)) { g2.setStroke(new BasicStroke(2.5f)); Path2D p = new Path2D.Float(); p.moveTo(2, size/2); p.lineTo(size/2 - 2, size - 3); p.lineTo(size - 2, 3); g2.draw(p); } g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}