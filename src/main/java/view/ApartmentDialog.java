package view;

import dao.FloorDAO;
import model.Apartment;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ApartmentDialog extends JDialog {

    private FloorDAO floorDAO;
    private JComboBox<Floor> cbbFloor;
    private JTextField txtRoomNumber;
    private JTextField txtArea;
    private JTextField txtPrice;
    
    private JComboBox<String> cbbType;      
    private JSpinner spnBedrooms;           
    private JSpinner spnBathrooms;          
    
    private JComboBox<String> cbbStatus;
    private JTextArea txtDesc;
    
    private boolean confirmed = false;
    private Apartment apartment;
    private Long buildingId;

    public ApartmentDialog(Frame owner, Apartment apartment, Long buildingId) {
        super(owner, apartment.getId() == null ? "Thêm Căn Hộ Mới" : "Cập Nhật Căn Hộ", true);
        this.apartment = apartment;
        this.buildingId = buildingId;
        this.floorDAO = new FloorDAO();
        
        initUI();
        fillData();
        
        setSize(850, 650); 
        setLocationRelativeTo(owner);
        setResizable(true); 
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        // === 1. HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        JLabel titleLabel = new JLabel(apartment.getId() == null ? "THIẾT LẬP CĂN HỘ MỚI" : "THÔNG TIN CHI TIẾT CĂN HỘ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === 2. BODY WITH SCROLLPANE ===
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(new Color(245, 245, 250));
        bodyPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        // --- SECTION 1: Vị Trí & Loại Hình ---
        JPanel pnlLocation = createSectionPanel("Vị Trí & Loại Hình");
        cbbFloor = new JComboBox<>();
        List<Floor> floors = floorDAO.getFloorsByBuildingId(buildingId);
        for (Floor f : floors) cbbFloor.addItem(f);
        styleComboBox(cbbFloor);
        cbbFloor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Floor) setText(((Floor) value).getName());
                return this;
            }
        });

        txtRoomNumber = createRoundedField();
        cbbType = new JComboBox<>(new String[]{"Standard", "Studio", "Mini", "Duplex", "Penthouse", "Shophouse"});
        styleComboBox(cbbType);

        JPanel row1 = new JPanel(new GridLayout(1, 3, 20, 0)); 
        row1.setOpaque(false);
        row1.add(createFieldGroup("Thuộc Tầng (*)", cbbFloor));
        row1.add(createFieldGroup("Số Phòng / Mã (*)", txtRoomNumber));
        row1.add(createFieldGroup("Loại Căn Hộ", cbbType));
        pnlLocation.add(row1);

        // --- SECTION 2: Thông Số Chi Tiết ---
        JPanel pnlSpecs = createSectionPanel("Thông Số Chi Tiết");
        txtArea = createRoundedField();
        spnBedrooms = createSpinner();
        spnBathrooms = createSpinner();
        
        JPanel row2 = new JPanel(new GridLayout(1, 3, 20, 0));
        row2.setOpaque(false);
        row2.add(createFieldGroup("Diện Tích (m²)", txtArea));
        row2.add(createFieldGroup("Phòng Ngủ", spnBedrooms));
        row2.add(createFieldGroup("Phòng Tắm", spnBathrooms));
        pnlSpecs.add(row2);

        // --- SECTION 3: Tài Chính & Trạng Thái ---
        JPanel pnlFinance = createSectionPanel("Tài Chính & Trạng Thái");
        txtPrice = createRoundedField();
        
        // [MỚI] Chỉ cho phép chọn Trống hoặc Bảo trì khi nhập liệu thủ công
        cbbStatus = new JComboBox<>(new String[]{"Trống", "Bảo trì"});
        styleComboBox(cbbStatus);
        
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setOpaque(false);
        row3.add(createFieldGroup("Giá Thuê Cơ Bản (VNĐ)", txtPrice));
        row3.add(createFieldGroup("Trạng Thái Hoạt Động", cbbStatus));
        pnlFinance.add(row3);

        // --- SECTION 4: MÔ TẢ ---
        JPanel pnlDesc = createSectionPanel("Thông Tin Bổ Sung");
        txtDesc = new JTextArea(6, 20); 
        txtDesc.setFont(UIConstants.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setPreferredSize(new Dimension(0, 120)); 
        scrollDesc.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        pnlDesc.add(createFieldGroup("Mô Tả Tiện Ích & Ghi Chú", scrollDesc));

        bodyPanel.add(pnlLocation);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(pnlSpecs);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(pnlFinance);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(pnlDesc);

        JScrollPane mainScroll = new JScrollPane(bodyPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        // === 3. FOOTER ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = new RoundedButton("Hủy Bỏ", 10);
        btnCancel.setBackground(new Color(245, 245, 245));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new RoundedButton("Lưu Dữ Liệu", 10);
        btnSave.setBackground(UIConstants.PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> onSave());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillData() {
        if (apartment.getId() != null) {
            txtRoomNumber.setText(apartment.getRoomNumber());
            if (apartment.getArea() != null) txtArea.setText(String.valueOf(apartment.getArea()));
            if (apartment.getBasePrice() != null) txtPrice.setText(apartment.getBasePrice().toString());
            if (apartment.getApartmentType() != null) cbbType.setSelectedItem(apartment.getApartmentType());
            if (apartment.getBedroomCount() != null) spnBedrooms.setValue(apartment.getBedroomCount());
            if (apartment.getBathroomCount() != null) spnBathrooms.setValue(apartment.getBathroomCount());
            txtDesc.setText(apartment.getDescription());
            
            // [MỚI] Xử lý hiển thị trạng thái "Đã thuê"
            String status = apartment.getStatus();
            if ("RENTED".equals(status)) {
                cbbStatus.addItem("Đã thuê");
                cbbStatus.setSelectedItem("Đã thuê");
                cbbStatus.setEnabled(false); // Khóa ComboBox nếu đã thuê
            } else if ("AVAILABLE".equals(status)) {
                cbbStatus.setSelectedItem("Trống");
                cbbStatus.setEnabled(true);
            } else if ("MAINTENANCE".equals(status)) {
                cbbStatus.setSelectedItem("Bảo trì");
                cbbStatus.setEnabled(true);
            }
            
            if (apartment.getFloorId() != null) {
                for (int i = 0; i < cbbFloor.getItemCount(); i++) {
                    Floor f = cbbFloor.getItemAt(i);
                    if (f.getId().equals(apartment.getFloorId())) {
                        cbbFloor.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void onSave() {
        if (txtRoomNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Số phòng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Double area = txtArea.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtArea.getText().trim());
            BigDecimal price = txtPrice.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtPrice.getText().trim());
            
            apartment.setFloorId(((Floor) cbbFloor.getSelectedItem()).getId());
            apartment.setRoomNumber(txtRoomNumber.getText().trim());
            apartment.setArea(area);
            apartment.setBasePrice(price);
            apartment.setDescription(txtDesc.getText().trim());
            
            // [MỚI] Ánh xạ trạng thái để lưu DB
            String selectedStatus = (String) cbbStatus.getSelectedItem();
            if ("Trống".equals(selectedStatus)) apartment.setStatus("AVAILABLE");
            else if ("Bảo trì".equals(selectedStatus)) apartment.setStatus("MAINTENANCE");
            else if ("Đã thuê".equals(selectedStatus)) apartment.setStatus("RENTED");

            apartment.setApartmentType((String) cbbType.getSelectedItem());
            apartment.setBedroomCount((Integer) spnBedrooms.getValue());
            apartment.setBathroomCount((Integer) spnBathrooms.getValue());
            
            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helpers UI ---
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
    
    private void styleComboBox(JComboBox box) {
        box.setPreferredSize(new Dimension(100, 35));
        box.setBackground(Color.WHITE);
        box.setFont(UIConstants.FONT_REGULAR);
    }
    
    private JSpinner createSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        spinner.setPreferredSize(new Dimension(100, 35));
        spinner.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        return spinner;
    }
    
    private JTextField createRoundedField() {
        JTextField f = new RoundedTextField(8);
        f.setPreferredSize(new Dimension(100, 35));
        f.setFont(UIConstants.FONT_REGULAR);
        return f;
    }

    public boolean isConfirmed() { return confirmed; }
    public Apartment getApartment() { return apartment; }

    // Inner Classes Styles
    private static class RoundedTextField extends JTextField { 
        private int arc; 
        public RoundedTextField(int arc) { this.arc = arc; setOpaque(false); setBorder(new EmptyBorder(5, 10, 5, 10)); } 
        @Override protected void paintComponent(Graphics g) { 
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2.setColor(Color.WHITE); g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc); 
            g2.setColor(new Color(180, 180, 180)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc); 
            super.paintComponent(g); g2.dispose(); 
        } 
    }

    private static class RoundedButton extends JButton { 
        private int arc; 
        public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 13)); } 
        @Override protected void paintComponent(Graphics g) { 
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); 
            super.paintComponent(g); g2.dispose(); 
        } 
    }
}