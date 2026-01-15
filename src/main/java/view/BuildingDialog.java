package view;

import model.Building;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Path2D;

public class BuildingDialog extends JDialog {

    private JTextField txtName, txtAddress, txtManager;
    private JComboBox<String> cbbStatus;
    private JTextArea txtDesc;
    
    private boolean confirmed = false;
    private Building building;

    public BuildingDialog(Frame owner, Building building) {
        super(owner, building == null || building.getId() == null ? "Thêm Mới Tòa Nhà" : "Chi Tiết Tòa Nhà", true);
        this.building = (building == null) ? new Building() : building;
        
        initUI();
        fillData();
        
        // Tăng chiều cao lên 680 để vừa với ô mô tả lớn hơn
        setSize(650, 680); 
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250)); 

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(Color.WHITE); 
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); 
        
        JLabel iconLabel = new JLabel(new SimpleIcon("BUILDING_BIG", 32, UIConstants.PRIMARY_COLOR));
        JLabel titleLabel = new JLabel(building.getId() == null ? "THIẾT LẬP TÒA NHÀ MỚI" : "CẬP NHẬT THÔNG TIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === 2. BODY ===
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(new Color(245, 245, 250));
        bodyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // KHỞI TẠO COMPONENTS
        txtName = createRoundedField();
        txtAddress = createRoundedField();
        txtManager = createRoundedField();
        
        cbbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì"});
        cbbStatus.setFont(UIConstants.FONT_REGULAR);
        cbbStatus.setBackground(Color.WHITE);
        ((JComponent)cbbStatus.getRenderer()).setBorder(new EmptyBorder(0, 5, 0, 0));
        cbbStatus.setPreferredSize(new Dimension(200, 35));

        // --- TĂNG KÍCH THƯỚC Ô MÔ TẢ TẠI ĐÂY ---
        txtDesc = new JTextArea(8, 20); // Tăng từ 4 lên 8 dòng
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // --- SECTION 1: THÔNG TIN CHÍNH ---
        JPanel pnlGeneral = createSectionPanel("Thông Tin Cơ Bản");
        
        JPanel row1 = new JPanel(new GridLayout(1, 2, 15, 0)); 
        row1.setOpaque(false);
        row1.add(createFieldGroup("Tên Tòa Nhà (*)", txtName));
        row1.add(createFieldGroup("Trạng Thái", cbbStatus));
        
        pnlGeneral.add(row1);
        pnlGeneral.add(Box.createVerticalStrut(15));
        pnlGeneral.add(createFieldGroup("Địa Chỉ Chi Tiết (*)", txtAddress));

        // --- SECTION 2: QUẢN LÝ & GHI CHÚ ---
        JPanel pnlExtra = createSectionPanel("Quản Lý & Ghi Chú");
        
        pnlExtra.add(createFieldGroup("Người Quản Lý", txtManager));
        pnlExtra.add(Box.createVerticalStrut(15));
        pnlExtra.add(createFieldGroup("Mô Tả Thêm", scrollDesc));

        // Thêm vào body
        bodyPanel.add(pnlGeneral);
        bodyPanel.add(Box.createVerticalStrut(15)); 
        bodyPanel.add(pnlExtra);
        
        add(bodyPanel, BorderLayout.CENTER);

        // === 3. FOOTER ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE); 
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230))); 

        JButton btnCancel = new RoundedButton("Đóng", 10);
        btnCancel.setBackground(new Color(245, 245, 245));
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new RoundedButton("Lưu Thay Đổi", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setIcon(new SimpleIcon("CHECK", 12, Color.WHITE));
        btnSave.setPreferredSize(new Dimension(140, 38));
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        TitledBorder border = BorderFactory.createTitledBorder(
            new LineBorder(new Color(220, 220, 220), 1, true), 
            title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        border.setTitleColor(UIConstants.PRIMARY_COLOR);
        
        panel.setBorder(new CompoundBorder(border, new EmptyBorder(15, 20, 15, 20)));
        return panel;
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 8)); 
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80)); 
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void fillData() {
        if (building.getId() != null) {
            txtName.setText(building.getName());
            txtAddress.setText(building.getAddress());
            txtManager.setText(building.getManagerName());
            txtDesc.setText(building.getDescription());
            if (building.getStatus() != null) cbbStatus.setSelectedItem(building.getStatus());
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String addr = txtAddress.getText().trim();

        if (name.isEmpty() || addr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên tòa nhà và Địa chỉ không được để trống!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        building.setName(name);
        building.setAddress(addr);
        building.setManagerName(txtManager.getText().trim());
        building.setDescription(txtDesc.getText().trim());
        building.setStatus((String) cbbStatus.getSelectedItem());
        
        if (building.getId() == null) {
            building.setDeleted(false);
            
        }

        confirmed = true;
        dispose();
    }

    // --- Helpers UI Components ---
    public boolean isConfirmed() { return confirmed; }
    public Building getBuilding() { return building; }
    
    private JTextField createRoundedField() { 
        JTextField f = new RoundedTextField(8); 
        f.setFont(UIConstants.FONT_REGULAR); 
        f.setPreferredSize(new Dimension(100, 35)); 
        return f; 
    }
    
    // --- Styles ---
    private static class RoundedTextField extends JTextField { 
        private int arc; public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 10, 5, 10)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc); g2.setColor(new Color(180, 180, 180)); g2.setStroke(new BasicStroke(1.0f)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc); super.paintComponent(g); g2.dispose(); } 
    }
    private static class RoundedButton extends JButton { 
        private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); if (getModel().isArmed()) g2.setColor(getBackground().darker()); else g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); } 
    }
    private static class SimpleIcon implements Icon { 
        private String type; private int size; private Color color; public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; } 
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.setStroke(new BasicStroke(1.5f)); g2.translate(x, y); 
            if ("BUILDING_BIG".equals(type)) { 
                int baseY = size - 4; int midW = size * 36 / 100; int midH = size * 80 / 100; int midX = (size - midW) / 2; g2.fillRect(midX, baseY - midH, midW, midH); int leftW = size * 15 / 100; int leftH = size * 40 / 100; int leftX = midX - leftW - 2; g2.drawRect(leftX, baseY - leftH, leftW, leftH); g2.fillRect(leftX + leftW, baseY - leftH + 5, 2, leftH - 5); int rightW = size * 18 / 100; int rightH = size * 60 / 100; int rightX = midX + midW + 2; g2.drawRect(rightX, baseY - rightH, rightW, rightH); g2.setColor(Color.WHITE); int winSize = size/12; int gap = size/16; int startWX = midX + (midW - (3*winSize + 2*gap))/2; int startWY = baseY - midH + size/6; for(int r=0; r<4; r++) for(int c1=0; c1<3; c1++) g2.fillRect(startWX + c1*(winSize+gap), startWY + r*(winSize+gap), winSize, winSize); 
            } else if ("CHECK".equals(type)) { 
                g2.setStroke(new BasicStroke(2.5f)); java.awt.geom.Path2D p = new java.awt.geom.Path2D.Float(); p.moveTo(2, size/2); p.lineTo(size/2 - 2, size - 3); p.lineTo(size - 2, 3); g2.draw(p); 
            } g2.dispose(); 
        } 
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; } 
    }
}