package view;

import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FloorDialog extends JDialog {

    private JTextField txtFloorNumber; 
    private JTextField txtName;        
    private JComboBox<String> cbbStatus; 
    private JTextArea txtDesc;          
    
    private boolean confirmed = false;
    private Floor floor;
    private boolean dataChanged = false; // Biến cờ theo dõi thay đổi

    public FloorDialog(Frame owner, Floor floor) {
        super(owner, floor.getId() == null ? "Thêm Tầng Mới" : "Cập Nhật Tầng", true);
        this.floor = floor;
        
        initUI();
        fillData();
        
        // Reset cờ sau khi nạp dữ liệu xong
        dataChanged = false;
        
        setSize(450, 580); 
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // HEADER
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(new SimpleIcon("FLOOR_STACK", 45, UIConstants.PRIMARY_COLOR));
        JLabel titleLabel = new JLabel(floor.getId() == null ? "THÊM TẦNG MỚI" : "CẬP NHẬT TẦNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // BODY
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        txtFloorNumber = createRoundedField();
        txtName = createRoundedField();
        
        cbbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì"});
        cbbStatus.setFont(UIConstants.FONT_REGULAR);
        cbbStatus.setBackground(Color.WHITE);
        cbbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbbStatus.setPreferredSize(new Dimension(100, 35));
        cbbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        ((JComponent)cbbStatus.getRenderer()).setBorder(new EmptyBorder(5,5,5,5));

        txtDesc = new JTextArea(5, 20);
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- LẮNG NGHE THAY ĐỔI DỮ LIỆU ---
        SimpleDocumentListener docListener = new SimpleDocumentListener(() -> dataChanged = true);
        txtFloorNumber.getDocument().addDocumentListener(docListener);
        txtName.getDocument().addDocumentListener(docListener);
        txtDesc.getDocument().addDocumentListener(docListener);
        cbbStatus.addActionListener(e -> dataChanged = true);

        formPanel.add(createLabel("Số Tầng (VD: 1, 2...) (*)"));
        formPanel.add(txtFloorNumber);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createLabel("Tên Hiển Thị (VD: Tầng 1) (*)"));
        formPanel.add(txtName);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createLabel("Trạng Thái Hoạt Động"));
        formPanel.add(cbbStatus);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createLabel("Mô Tả / Ghi Chú"));
        formPanel.add(scrollDesc);
        
        add(formPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 248, 248));

        JButton btnCancel = new RoundedButton("Hủy Bỏ", 10);
        btnCancel.setBackground(new Color(225, 225, 225));
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> handleCancel()); // Gọi hàm xử lý đóng

        JButton btnSave = new RoundedButton("Lưu Lại", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(120, 38));
        btnSave.addActionListener(e -> onSave()); // Gọi hàm xử lý lưu

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        // Xử lý nút X trên cửa sổ
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });
    }

    // --- XỬ LÝ ĐÓNG FORM (Có hỏi xác nhận) ---
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

    private void fillData() {
        if (floor.getId() != null) {
            txtFloorNumber.setText(String.valueOf(floor.getFloorNumber()));
            txtName.setText(floor.getName());
            if (floor.getStatus() != null) {
                cbbStatus.setSelectedItem(floor.getStatus());
            }
        }
    }

    // --- XỬ LÝ LƯU (Có hỏi xác nhận) ---
    private void onSave() {
        String numStr = txtFloorNumber.getText().trim();
        String name = txtName.getText().trim();

        if (numStr.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Số tầng và Tên tầng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int num;
        try {
            num = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tầng phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // KIỂM TRA TRÙNG LẶP
        dao.FloorDAO dao = new dao.FloorDAO();
        if (floor.getId() == null) {
            if (dao.isFloorNameExists(floor.getBuildingId(), name)) {
                JOptionPane.showMessageDialog(this, "Tên tầng '" + name + "' đã tồn tại trong tòa nhà này!", "Trùng dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // HỎI XÁC NHẬN TRƯỚC KHI LƯU
        int choice = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn lưu thông tin tầng này?", 
            "Xác nhận lưu", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return; // Hủy lưu
        }

        // Thực hiện lưu
        floor.setFloorNumber(num);
        floor.setName(name);
        floor.setStatus((String) cbbStatus.getSelectedItem());
        
        confirmed = true;
        dataChanged = false;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Floor getFloor() { return floor; }

    // Helpers UI
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

    // Class lắng nghe thay đổi
    private static class SimpleDocumentListener implements DocumentListener {
        private Runnable onChange;
        public SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
    }

    // Styles
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
            if ("FLOOR_STACK".equals(type)) {
                int h = size/4; int w = size*2/3; int cx = (size-w)/2;
                g2.fillRoundRect(cx, size-h, w, h, 4, 4);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                g2.fillRoundRect(cx, size-h*2+4, w, h, 4, 4);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
                g2.fillRoundRect(cx, size-h*3+8, w, h, 4, 4);
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}