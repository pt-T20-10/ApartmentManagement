package view;

import dao.FloorDAO;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.border.TitledBorder;

public class FloorDialog extends JDialog {

    private FloorDAO floorDAO;
    private Floor floor;
    private boolean confirmed = false;
    private boolean dataChanged = false;

    private JTextField txtName;
    private JTextField txtNumber; 
    private JTextArea txtDesc;

    public FloorDialog(Frame owner, Floor floor) {
        super(owner, floor.getId() == null ? "Thêm Tầng Mới" : "Cập Nhật Tầng", true);
        this.floor = floor;
        this.floorDAO = new FloorDAO();

        initUI();
        fillData();

        dataChanged = false; 
        
        setSize(500, 450); 
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        // HEADER
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        JLabel titleLabel = new JLabel(floor.getId() == null ? "THIẾT LẬP TẦNG MỚI" : "THÔNG TIN TẦNG");
        titleLabel.setFont(UIConstants.FONT_HEADING);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // BODY
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(new Color(245, 245, 250));
        bodyPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel pnlInfo = createSectionPanel("Thông Tin Chung");
        txtName = createRoundedField();
        txtNumber = createRoundedField(); 
        
        JPanel gridPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        gridPanel.setOpaque(false);
        gridPanel.add(createFieldGroup("Tên Tầng (VD: Tầng 1) (*)", txtName));
        gridPanel.add(createFieldGroup("Số Thứ Tự Tầng (Số nguyên) (*)", txtNumber));
        pnlInfo.add(gridPanel);

        JPanel pnlDesc = createSectionPanel("Mô Tả / Ghi Chú");
        txtDesc = new JTextArea(4, 20);
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        pnlDesc.add(createFieldGroup("Ghi chú thêm", scrollDesc));

        SimpleDocumentListener docListener = new SimpleDocumentListener(() -> dataChanged = true);
        txtName.getDocument().addDocumentListener(docListener);
        txtNumber.getDocument().addDocumentListener(docListener);
        txtDesc.getDocument().addDocumentListener(docListener);

        bodyPanel.add(pnlInfo);
        bodyPanel.add(Box.createVerticalStrut(15));
        bodyPanel.add(pnlDesc);
        add(bodyPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = new RoundedButton("Hủy Bỏ", 10);
        btnCancel.setBackground(new Color(245, 245, 245));
        btnCancel.addActionListener(e -> handleCancel());

        JButton btnSave = new RoundedButton("Lưu Tầng", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        configureShortcuts(btnSave);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleCancel(); }
        });
    }

    private void configureShortcuts(JButton defaultButton) {
        getRootPane().setDefaultButton(defaultButton);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handleCancel(); }
        });
    }

    private void handleCancel() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn thoát? Dữ liệu chưa lưu sẽ bị mất.",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) dispose();
    }

    private void onSave() {
        // Validation
        if (txtName.getText().trim().isEmpty() || txtNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên tầng và Số thứ tự!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // [MỚI] HỎI XÁC NHẬN TRƯỚC KHI LƯU
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc chắn muốn lưu thông tin tầng này không?", 
            "Xác nhận lưu", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        // Nếu người dùng chọn NO hoặc tắt dialog -> Dừng lại, không lưu
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int floorNum = Integer.parseInt(txtNumber.getText().trim());
            
            floor.setName(txtName.getText().trim());
            floor.setFloorNumber(floorNum);
            floor.setDescription(txtDesc.getText().trim());
            // buildingId đã có sẵn trong object floor

            confirmed = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số thứ tự tầng phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillData() {
        if (floor.getId() != null) {
            txtName.setText(floor.getName());
            txtNumber.setText(String.valueOf(floor.getFloorNumber()));
            txtDesc.setText(floor.getDescription());
        } else {
            txtNumber.setText(""); 
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public Floor getFloor() { return floor; }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(new Color(220, 220, 220), 1, true), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        border.setTitleColor(UIConstants.PRIMARY_COLOR);
        panel.setBorder(new CompoundBorder(border, new EmptyBorder(10, 15, 10, 15)));
        return panel;
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createRoundedField() {
        JTextField f = new RoundedTextField(8);
        f.setPreferredSize(new Dimension(100, 35));
        f.setFont(UIConstants.FONT_REGULAR);
        return f;
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private Runnable onChange;
        public SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
    }

    private static class RoundedTextField extends JTextField { 
        private int arc; public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 10, 5, 10)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc); g2.setColor(new Color(180, 180, 180)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc); super.paintComponent(g); g2.dispose(); } 
    }
    
    private static class RoundedButton extends JButton { 
        private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); } 
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); } 
    }
}