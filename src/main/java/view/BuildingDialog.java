package view;

import model.Building;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder; // Đã thêm import này
import java.awt.*;
import java.awt.geom.Path2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BuildingDialog extends JDialog {

    private JTextField txtName, txtAddress, txtManager, txtDate;
    private JComboBox<String> cbbStatus;
    private JTextArea txtDesc;
    
    private boolean confirmed = false;
    private Building building;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public BuildingDialog(Frame owner, Building building) {
        super(owner, building == null || building.getId() == null ? "Thêm Mới" : "Chi Tiết", true);
        this.building = (building == null) ? new Building() : building;
        
        initUI();
        fillData();
        
        setSize(700, 580); 
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(240, 247, 255)); 
        // Sử dụng MatteBorder ở đây
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); 
        
        JLabel iconLabel = new JLabel(new SimpleIcon("BUILDING_BIG", 32, UIConstants.PRIMARY_COLOR));
        JLabel titleLabel = new JLabel(building.getId() == null ? "THÊM TÒA NHÀ MỚI" : "THÔNG TIN CHI TIẾT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === 2. FORM BODY ===
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        // KHỞI TẠO COMPONENTS
        txtName = createRoundedField();
        txtAddress = createRoundedField();
        txtManager = createRoundedField();
        txtDate = createRoundedField();
        
        cbbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì", "Dừng hoạt động"});
        cbbStatus.setFont(UIConstants.FONT_REGULAR);
        cbbStatus.setBackground(Color.WHITE);
        ((JComponent)cbbStatus.getRenderer()).setBorder(new EmptyBorder(5,5,5,5));

        // --- HÀNG 1: TÊN (50%) | QUẢN LÝ (50%) ---
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setBackground(Color.WHITE);
        row1.add(createFieldGroup("Tên Tòa Nhà (*)", txtName));
        row1.add(createFieldGroup("Người Quản Lý", txtManager));
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        
        contentPanel.add(row1);
        contentPanel.add(Box.createVerticalStrut(15)); 

        // --- HÀNG 2: ĐỊA CHỈ (Full Width) ---
        contentPanel.add(createFieldGroup("Địa Chỉ (*)", txtAddress));
        contentPanel.add(Box.createVerticalStrut(15));

        // --- HÀNG 3: TRẠNG THÁI (50%) | NGÀY HOẠT ĐỘNG (50%) ---
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setBackground(Color.WHITE);
        
        JPanel statusWrapper = new JPanel(new BorderLayout());
        statusWrapper.setBackground(Color.WHITE);
        statusWrapper.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        statusWrapper.add(cbbStatus);
        
        row3.add(createFieldGroup("Trạng Thái", statusWrapper));
        row3.add(createFieldGroup("Ngày Hoạt Động (yyyy-MM-dd)", txtDate));
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        
        contentPanel.add(row3);
        contentPanel.add(Box.createVerticalStrut(15));

        // --- HÀNG 4: MÔ TẢ (Full Width) ---
        txtDesc = new JTextArea(4, 20); 
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(200, 200, 200), 1)); 
        
        contentPanel.add(createFieldGroup("Mô Tả / Ghi Chú", scrollDesc));
        
        add(contentPanel, BorderLayout.CENTER);

        // === 3. FOOTER ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(250, 250, 250)); 
        // Sử dụng MatteBorder ở đây
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230))); 

        JButton btnCancel = new RoundedButton("Đóng", 10);
        btnCancel.setBackground(new Color(245, 245, 245));
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new RoundedButton("Lưu Lại", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setIcon(new SimpleIcon("CHECK", 12, Color.WHITE));
        btnSave.setPreferredSize(new Dimension(120, 38));
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6)); 
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(100, 100, 100)); 
        
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
            cbbStatus.setSelectedItem(building.getStatus());
            if (building.getOperationDate() != null) {
                txtDate.setText(dateFormat.format(building.getOperationDate()));
            }
        } else {
            txtDate.setText(dateFormat.format(new java.util.Date()));
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String addr = txtAddress.getText().trim();
        String dateStr = txtDate.getText().trim();

        if (name.isEmpty() || addr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên và Địa chỉ!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (!dateStr.isEmpty()) {
                java.util.Date parsed = dateFormat.parse(dateStr);
                building.setOperationDate(new java.sql.Date(parsed.getTime()));
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sai định dạng yyyy-MM-dd", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        building.setName(name);
        building.setAddress(addr);
        building.setManagerName(txtManager.getText().trim());
        building.setDescription(txtDesc.getText().trim());
        building.setStatus((String) cbbStatus.getSelectedItem());
        if (building.getId() == null) building.setDeleted(false); 

        confirmed = true;
        dispose();
    }

    // --- Helpers UI Components ---
    public boolean isConfirmed() { return confirmed; }
    public Building getBuilding() { return building; }
    
    private JTextField createRoundedField() { 
        JTextField f = new RoundedTextField(10); 
        f.setFont(UIConstants.FONT_REGULAR); 
        f.setPreferredSize(new Dimension(100, 38)); 
        return f; 
    }
    
    private static class RoundedTextField extends JTextField { private int arc; public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 12, 5, 12)); } @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc); g2.setColor(new Color(200, 200, 200)); g2.setStroke(new BasicStroke(1.0f)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc); super.paintComponent(g); g2.dispose(); } }
    private static class RoundedButton extends JButton { private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); } @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); if (getModel().isArmed()) g2.setColor(getBackground().darker()); else g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); } }
    private static class SimpleIcon implements Icon { private String type; private int size; private Color color; public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; } @Override public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.setStroke(new BasicStroke(1.5f)); g2.translate(x, y); if ("BUILDING_BIG".equals(type)) { int baseY = size - 4; int midW = size * 36 / 100; int midH = size * 80 / 100; int midX = (size - midW) / 2; g2.fillRect(midX, baseY - midH, midW, midH); int leftW = size * 15 / 100; int leftH = size * 40 / 100; int leftX = midX - leftW - 2; g2.drawRect(leftX, baseY - leftH, leftW, leftH); g2.fillRect(leftX + leftW, baseY - leftH + 5, 2, leftH - 5); int rightW = size * 18 / 100; int rightH = size * 60 / 100; int rightX = midX + midW + 2; g2.drawRect(rightX, baseY - rightH, rightW, rightH); g2.setColor(Color.WHITE); int winSize = size/12; int gap = size/16; int startWX = midX + (midW - (3*winSize + 2*gap))/2; int startWY = baseY - midH + size/6; for(int r=0; r<4; r++) for(int c1=0; c1<3; c1++) g2.fillRect(startWX + c1*(winSize+gap), startWY + r*(winSize+gap), winSize, winSize); } else if ("CHECK".equals(type)) { g2.setStroke(new BasicStroke(2.5f)); Path2D p = new Path2D.Float(); p.moveTo(2, size/2); p.lineTo(size/2 - 2, size - 3); p.lineTo(size - 2, 3); g2.draw(p); } g2.dispose(); } @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; } }
}