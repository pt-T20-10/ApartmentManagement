package view;

import dao.ApartmentDAO;
import dao.BuildingDAO;
import dao.FloorDAO;
import model.Apartment;
import model.Building;
import model.Floor;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ApartmentManagementPanel extends JPanel {

    private ApartmentDAO apartmentDAO;
    private BuildingDAO buildingDAO;
    private FloorDAO floorDAO;

    private JComboBox<Building> cbbBuilding;
    private JComboBox<Floor> cbbFloor;
    private JComboBox<String> cbbStatusFilter;
    private JPanel cardsContainer;

    private Building currentBuilding;
    private Floor currentFloor;

    public ApartmentManagementPanel() {
        this.apartmentDAO = new ApartmentDAO();
        this.buildingDAO = new BuildingDAO();
        this.floorDAO = new FloorDAO();

        initUI();
        loadBuildingData();
    }

    public void setFloor(Floor floor) {
        if (floor == null || floor.getBuildingId() == null) return;

        for (int i = 0; i < cbbBuilding.getItemCount(); i++) {
            Building b = cbbBuilding.getItemAt(i);
            if (b != null && b.getId() != null && b.getId().equals(floor.getBuildingId())) {
                cbbBuilding.setSelectedIndex(i);
                break;
            }
        }

        SwingUtilities.invokeLater(() -> {
            for (int j = 0; j < cbbFloor.getItemCount(); j++) {
                Floor f = cbbFloor.getItemAt(j);
                if (f != null && f.getId() != null && f.getId().equals(floor.getId())) {
                    cbbFloor.setSelectedIndex(j);
                    break;
                }
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // === HEADER PANEL ===
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // Row 1: Title + Button
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(UIConstants.BACKGROUND_COLOR);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel leftRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftRow1.setBackground(UIConstants.BACKGROUND_COLOR);

        JButton btnBack = createBackArrowButton();
        btnBack.addActionListener(e -> {
            MainDashboard main = (MainDashboard) SwingUtilities.getWindowAncestor(this);
            if (currentBuilding != null && main != null) {
                main.showFloorsOfBuilding(currentBuilding);
            }
        });
        leftRow1.add(btnBack);

        JLabel lblTitle = new JLabel("Quản Lý Căn Hộ");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        leftRow1.add(lblTitle);

        JPanel rightRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightRow1.setBackground(UIConstants.BACKGROUND_COLOR);

        JButton btnAdd = new RoundedButton(" Thêm Căn Hộ", 15);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.addActionListener(e -> showAddDialog());
        rightRow1.add(btnAdd);

        row1.add(leftRow1, BorderLayout.WEST);
        row1.add(rightRow1, BorderLayout.EAST);

        // Row 2: Filters
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        row2.setBackground(UIConstants.BACKGROUND_COLOR);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblBuilding = new JLabel("Tòa nhà:");
        lblBuilding.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBuilding.setForeground(UIConstants.TEXT_PRIMARY);
        row2.add(lblBuilding);

        cbbBuilding = new JComboBox<>();
        cbbBuilding.setPreferredSize(new Dimension(250, 35));
        cbbBuilding.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbBuilding.setBackground(Color.WHITE);
        cbbBuilding.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Building) setText(((Building) value).getName());
                return this;
            }
        });
        cbbBuilding.addActionListener(e -> onBuildingChanged());
        row2.add(cbbBuilding);

        JLabel lblFloor = new JLabel("Tầng:");
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFloor.setForeground(UIConstants.TEXT_PRIMARY);
        row2.add(lblFloor);

        cbbFloor = new JComboBox<>();
        cbbFloor.setPreferredSize(new Dimension(200, 35));
        cbbFloor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbFloor.setBackground(Color.WHITE);
        cbbFloor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Floor) setText(((Floor) value).getName());
                return this;
            }
        });
        cbbFloor.addActionListener(e -> loadApartments());
        row2.add(cbbFloor);

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(UIConstants.TEXT_PRIMARY);
        row2.add(lblStatus);

        cbbStatusFilter = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Trống", "Đã thuê", "Bảo trì"
        });
        cbbStatusFilter.setPreferredSize(new Dimension(180, 35));
        cbbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbStatusFilter.setBackground(Color.WHITE);
        cbbStatusFilter.addActionListener(e -> loadApartments());
        row2.add(cbbStatusFilter);

        headerPanel.add(row1);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(row2);

        add(headerPanel, BorderLayout.NORTH);

        // === CONTENT ===
        cardsContainer = new JPanel(new GridLayout(0, 4, 20, 20));
        cardsContainer.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(UIConstants.BACKGROUND_COLOR);
        contentWrapper.add(cardsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadBuildingData() {
        List<Building> buildings = buildingDAO.getAllBuildings();
        cbbBuilding.removeAllItems();
        for (Building b : buildings) cbbBuilding.addItem(b);
    }

    private void onBuildingChanged() {
        Building selected = (Building) cbbBuilding.getSelectedItem();
        cbbFloor.removeAllItems();
        if (selected != null && selected.getId() != null) {
            currentBuilding = selected;
            List<Floor> floors = floorDAO.getFloorsByBuildingId(selected.getId());
            cbbFloor.addItem(new Floor(null, 0, "Tất cả các tầng"));
            for (Floor f : floors) cbbFloor.addItem(f);
        }
        loadApartments();
    }

    private void loadApartments() {
        cardsContainer.removeAll();
        if (currentBuilding == null) {
            cardsContainer.revalidate();
            cardsContainer.repaint();
            return;
        }

        List<Apartment> list;
        Floor selectedFloor = (Floor) cbbFloor.getSelectedItem();
        if (selectedFloor != null && selectedFloor.getId() != null) {
            list = apartmentDAO.getApartmentsByFloorId(selectedFloor.getId());
            currentFloor = selectedFloor;
        } else {
            list = apartmentDAO.getApartmentsByBuildingId(currentBuilding.getId());
            currentFloor = null;
        }

        String statusFilter = (String) cbbStatusFilter.getSelectedItem();
        if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái")) {
            list = filterByStatus(list, statusFilter);
        }

        if (list.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có căn hộ nào");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(UIConstants.BACKGROUND_COLOR);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            cardsContainer.add(emptyPanel);
        } else {
            // [LOGIC MOCK DATA MỚI] 
            for (Apartment apt : list) {
                LocalDate mockEndDate = null;
                
                if ("RENTED".equalsIgnoreCase(apt.getStatus()) || "Đã thuê".equalsIgnoreCase(apt.getStatus())) {
                    try {
                        String numStr = apt.getRoomNumber().replaceAll("\\D+","");
                        int roomNum = numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
                        
                        // Lấy chữ số cuối cùng của số phòng (ví dụ 101 -> 1, 205 -> 5)
                        int lastDigit = roomNum % 10;

                        // [SỬA LOGIC]: 
                        // - Các căn có đuôi 1, 2, 3 (vd: 101, 102, 103): QUÁ HẠN (Đỏ)
                        // - Các căn có đuôi 4, 5: SẮP HẾT HẠN (Cam)
                        // - Còn lại: Bình thường
                        if (lastDigit >= 1 && lastDigit <= 3) {
                            mockEndDate = LocalDate.now().minusDays(5); // Quá hạn 5 ngày
                        } else if (lastDigit >= 4 && lastDigit <= 5) {
                            mockEndDate = LocalDate.now().plusDays(15); // Còn 15 ngày
                        } else {
                            mockEndDate = LocalDate.now().plusDays(200); // Còn lâu
                        }
                        
                    } catch (Exception e) { 
                        mockEndDate = LocalDate.now().plusDays(60); 
                    }
                }

                cardsContainer.add(new ApartmentCard(
                    apt, 
                    mockEndDate,
                    this::showQuickView,
                    this::editApartment,
                    this::deleteApartment
                ));
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void showQuickView(Apartment apt) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        Floor targetFloor = currentFloor;
        if (targetFloor == null) {
            targetFloor = floorDAO.getFloorById(apt.getFloorId());
        }
        if (targetFloor == null) {
            targetFloor = new Floor(); targetFloor.setName("Tầng ?");
        }

        ApartmentQuickViewDialog dialog = new ApartmentQuickViewDialog(
            parent, apt, currentBuilding, targetFloor, this::editApartment
        );
        dialog.setVisible(true);
    }

    private List<Apartment> filterByStatus(List<Apartment> apartments, String statusFilter) {
        List<Apartment> filtered = new ArrayList<>();
        for (Apartment apt : apartments) {
            String aptStatus = apt.getStatus();
            if (aptStatus == null) aptStatus = "AVAILABLE";
            boolean match = false;
            switch (statusFilter) {
                case "Trống": match = "AVAILABLE".equalsIgnoreCase(aptStatus) || "Trống".equalsIgnoreCase(aptStatus); break;
                case "Đã thuê": match = "RENTED".equalsIgnoreCase(aptStatus) || "Đã thuê".equalsIgnoreCase(aptStatus); break;
                case "Bảo trì": match = "MAINTENANCE".equalsIgnoreCase(aptStatus) || "Bảo trì".equalsIgnoreCase(aptStatus); break;
                default: match = true;
            }
            if (match) filtered.add(apt);
        }
        return filtered;
    }

    private void showAddDialog() {
        if (currentBuilding == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tòa nhà trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        Apartment newApt = new Apartment();
        if (currentFloor != null) newApt.setFloorId(currentFloor.getId());
        ApartmentDialog dialog = new ApartmentDialog(parent, newApt, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Apartment apt = dialog.getApartment();
            if (isRoomNumberExists(apt.getFloorId(), apt.getRoomNumber(), null)) {
                JOptionPane.showMessageDialog(this, "Số phòng " + apt.getRoomNumber() + " đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            if (apartmentDAO.insertApartment(apt)) { JOptionPane.showMessageDialog(this, "Thêm thành công!"); loadApartments(); } 
            else { JOptionPane.showMessageDialog(this, "Thêm thất bại!"); }
        }
    }

    private void editApartment(Apartment apt) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ApartmentDialog dialog = new ApartmentDialog(parent, apt, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Apartment updated = dialog.getApartment();
            if (isRoomNumberExists(updated.getFloorId(), updated.getRoomNumber(), apt.getId())) {
                JOptionPane.showMessageDialog(this, "Số phòng " + updated.getRoomNumber() + " đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            if (apartmentDAO.updateApartment(updated)) { JOptionPane.showMessageDialog(this, "Cập nhật thành công!"); loadApartments(); } 
            else { JOptionPane.showMessageDialog(this, "Cập nhật thất bại!"); }
        }
    }

    private void deleteApartment(Apartment apt) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa căn hộ " + apt.getRoomNumber() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (apartmentDAO.deleteApartment(apt.getId())) { JOptionPane.showMessageDialog(this, "Xóa thành công!"); loadApartments(); } 
            else { JOptionPane.showMessageDialog(this, "Xóa thất bại!"); }
        }
    }

    private boolean isRoomNumberExists(Long floorId, String roomNumber, Long excludeApartmentId) {
        if (floorId == null || roomNumber == null) return false;
        List<Apartment> apartments = apartmentDAO.getApartmentsByFloorId(floorId);
        for (Apartment apt : apartments) {
            if (excludeApartmentId != null && apt.getId().equals(excludeApartmentId)) continue;
            if (roomNumber.trim().equalsIgnoreCase(apt.getRoomNumber().trim())) return true;
        }
        return false;
    }

    private JButton createBackArrowButton() {
        JButton btn = new JButton("Quay lại");
        btn.setIcon(new HeaderIcon("BACK_ARROW", 16, UIConstants.PRIMARY_COLOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(UIConstants.PRIMARY_COLOR);
        btn.setBackground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 10, 5, 15)); 
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setContentAreaFilled(true); btn.setBackground(new Color(235, 245, 255)); }
            public void mouseExited(MouseEvent e) { btn.setContentAreaFilled(false); }
        });
        return btn;
    }

    private static class RoundedButton extends JButton {
        private int arc; public RoundedButton(String text, int arc) { super(text); this.arc = arc; setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); super.paintComponent(g); g2.dispose(); }
    }
    
    private static class HeaderIcon implements Icon {
        private String type; private int size; private Color color; public HeaderIcon(String type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { 
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2.setColor(color); g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.translate(x, y);
            if ("BACK_ARROW".equals(type)) { Path2D p = new Path2D.Float(); p.moveTo(size * 0.7, size * 0.2); p.lineTo(size * 0.3, size * 0.5); p.lineTo(size * 0.7, size * 0.8); g2.draw(p); }
            g2.dispose(); 
        } 
        @Override public int getIconWidth() { return size; } @Override public int getIconHeight() { return size; }
    }
}