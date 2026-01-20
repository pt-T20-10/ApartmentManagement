package view;

import model.Building;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;

public class BuildingDialog extends JDialog {

    private JTextField txtName, txtAddress, txtManager;
    private JComboBox<String> cbbStatus;
    private JTextArea txtDesc;
    
    private boolean confirmed = false;
    private Building building;
    private boolean dataChanged = false; // Biến cờ theo dõi thay đổi

    public BuildingDialog(Frame owner, Building building) {
        super(owner, building == null || building.getId() == null ? "Thêm Mới Tòa Nhà" : "Chi Tiết Tòa Nhà", true);
        this.building = (building == null) ? new Building() : building;
        
        initUI();
        fillData();
        
        // Reset cờ sau khi nạp dữ liệu xong
        dataChanged = false;
        
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

        // Khởi tạo Components
        txtName = createRoundedField();
        txtAddress = createRoundedField();
        txtManager = createRoundedField();
        
        cbbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì"});
        cbbStatus.setFont(UIConstants.FONT_REGULAR);
        cbbStatus.setBackground(Color.WHITE);
        cbbStatus.setPreferredSize(new Dimension(200, 35));

        txtDesc = new JTextArea(8, 20);
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // --- LẮNG NGHE THAY ĐỔI DỮ LIỆU ---
        SimpleDocumentListener docListener = new SimpleDocumentListener(() -> dataChanged = true);
        txtName.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
        txtManager.getDocument().addDocumentListener(docListener);
        txtDesc.getDocument().addDocumentListener(docListener);
        cbbStatus.addActionListener(e -> dataChanged = true);

        // SECTION 1
        JPanel pnlGeneral = createSectionPanel("Thông Tin Cơ Bản");
        JPanel row1 = new JPanel(new GridLayout(1, 2, 15, 0)); 
        row1.setOpaque(false);
        row1.add(createFieldGroup("Tên Tòa Nhà (*)", txtName));
        row1.add(createFieldGroup("Trạng Thái", cbbStatus));
        pnlGeneral.add(row1);
        pnlGeneral.add(Box.createVerticalStrut(15));
        pnlGeneral.add(createFieldGroup("Địa Chỉ Chi Tiết (*)", txtAddress));

        // SECTION 2
        JPanel pnlExtra = createSectionPanel("Quản Lý & Ghi Chú");
        pnlExtra.add(createFieldGroup("Người Quản Lý", txtManager));
        pnlExtra.add(Box.createVerticalStrut(15));
        pnlExtra.add(createFieldGroup("Mô Tả Thêm", scrollDesc));

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
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> handleCancel());

        JButton btnSave = new RoundedButton("Lưu Thay Đổi", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setIcon(new SimpleIcon("CHECK", 12, Color.WHITE));
        btnSave.setPreferredSize(new Dimension(140, 38));
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        // Xử lý khi bấm nút X trên cửa sổ
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });
    }

    // --- XỬ LÝ NÚT ĐÓNG (Dùng JOptionPane thay vì ConfirmDialog) ---
    private void handleCancel() {
        if (dataChanged) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Dữ liệu chưa được lưu. Bạn có chắc muốn đóng?", 
                "Cảnh báo", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }

    // --- XỬ LÝ NÚT LƯU (Dùng JOptionPane thay vì ConfirmDialog) ---
    private void onSave() {
        String name = txtName.getText().trim();
        String addr = txtAddress.getText().trim();

        if (name.isEmpty() || addr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên tòa nhà và Địa chỉ không được để trống!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiện thông báo xác nhận bằng JOptionPane chuẩn
        String itemName = building.getId() == null ? "tòa nhà mới" : "thay đổi thông tin";
        int choice = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn lưu " + itemName + " này không?", 
            "Xác nhận lưu", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return; // Người dùng chọn No hoặc đóng dialog -> Không lưu
        }

        // Thực hiện lưu
        building.setName(name);
        building.setAddress(addr);
        building.setManagerName(txtManager.getText().trim());
        building.setDescription(txtDesc.getText().trim());
        building.setStatus((String) cbbStatus.getSelectedItem());
        
        if (building.getId() == null) {
            building.setDeleted(false);
        }

        confirmed = true;
        dataChanged = false;
        dispose();
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

    // --- Helper Classes ---
    
    // Class lắng nghe thay đổi text
    private static class SimpleDocumentListener implements DocumentListener {
        private Runnable onChange;
        public SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(new Color(220, 220, 220), 1, true), title);
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

    public boolean isConfirmed() { return confirmed; }
    public Building getBuilding() { return building; }
    
    private JTextField createRoundedField() { 
        JTextField f = new RoundedTextField(8); 
        f.setFont(UIConstants.FONT_REGULAR); 
        f.setPreferredSize(new Dimension(100, 35)); 
        return f; 
    }

    private static class RoundedTextField extends JTextField { 
        private int arc; public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 10, 5, 10)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc); g2.setColor(new Color(180, 180, 180)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc); super.paintComponent(g); g2.dispose(); } 
    }

    private static class RoundedButton extends JButton { 
        private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isArmed() ? getBackground().darker() : getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); } 
    }

    private static class SimpleIcon implements Icon { 
        private String type; private int size; private Color color; public SimpleIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; } 
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { 
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.translate(x, y); 
            if ("BUILDING_BIG".equals(type)) { 
                g2.fillRect(size/4, size/4, size/2, size/2); 
            } else if ("CHECK".equals(type)) { 
                g2.setStroke(new BasicStroke(2.5f)); Path2D p = new Path2D.Float(); p.moveTo(2, size/2); p.lineTo(size/2 - 2, size - 3); p.lineTo(size - 2, 3); g2.draw(p); 
            } g2.dispose(); 
        } 
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; } 
    }
}