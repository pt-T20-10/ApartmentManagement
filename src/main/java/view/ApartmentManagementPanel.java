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
import java.util.List;

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
        setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(UIConstants.BACKGROUND_COLOR);
        leftHeader.setPreferredSize(new Dimension(900, 40));

        JButton btnBack = createBackButton();
        btnBack.addActionListener(e -> {
            MainDashboard main = (MainDashboard) SwingUtilities.getWindowAncestor(this);
            if (currentBuilding != null && main != null) {
                main.showFloorsOfBuilding(currentBuilding);
            }
        });
        leftHeader.add(btnBack);

        JLabel lblTitle = new JLabel("Quản Lý Căn Hộ:");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        leftHeader.add(lblTitle);

        cbbBuilding = new JComboBox<>();
        cbbBuilding.setPreferredSize(new Dimension(260, 35));
        cbbBuilding.setMaximumSize(new Dimension(260, 35));
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

        cbbFloor = new JComboBox<>();
        cbbFloor.setPreferredSize(new Dimension(200, 35));
        cbbFloor.setMaximumSize(new Dimension(200, 35));
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

        cbbStatusFilter = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Trống", "Đã thuê", "Bảo trì"
        });
        cbbStatusFilter.setPreferredSize(new Dimension(200, 35));
        cbbStatusFilter.setMaximumSize(new Dimension(200, 35));
        cbbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbbStatusFilter.setBackground(Color.WHITE);
        cbbStatusFilter.addActionListener(e -> loadApartments());

        leftHeader.add(cbbBuilding);
        leftHeader.add(cbbFloor);
        leftHeader.add(cbbStatusFilter);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setBackground(UIConstants.BACKGROUND_COLOR);
        rightHeader.setPreferredSize(new Dimension(180, 40));

        JButton btnAdd = new RoundedButton(" Thêm Căn Hộ", 15);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.addActionListener(e -> showAddDialog());

        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        cardsContainer = new JPanel(new GridLayout(0, 4, 20, 20));
        cardsContainer.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(cardsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setBorder(null);
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
        if (currentBuilding == null) return;

        List<Apartment> list;
        Floor selectedFloor = (Floor) cbbFloor.getSelectedItem();
        if (selectedFloor != null && selectedFloor.getId() != null) {
            list = apartmentDAO.getApartmentsByFloorId(selectedFloor.getId());
            currentFloor = selectedFloor;
        } else {
            list = apartmentDAO.getApartmentsByBuildingId(currentBuilding.getId());
            currentFloor = null;
        }

        for (Apartment apt : list) {
            cardsContainer.add(new ApartmentCard(apt, this::editApartment, this::deleteApartment));
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void showAddDialog() {
        if (currentBuilding == null) return;
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        Apartment newApt = new Apartment();
        if (currentFloor != null) newApt.setFloorId(currentFloor.getId());
        ApartmentDialog dialog = new ApartmentDialog(parent, newApt, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (apartmentDAO.insertApartment(dialog.getApartment())) loadApartments();
        }
    }

    private void editApartment(Apartment apt) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ApartmentDialog dialog = new ApartmentDialog(parent, apt, currentBuilding.getId());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            if (apartmentDAO.updateApartment(dialog.getApartment())) loadApartments();
        }
    }

    private void deleteApartment(Apartment apt) {
        if (JOptionPane.showConfirmDialog(this, "Xóa căn hộ?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == 0) {
            if (apartmentDAO.deleteApartment(apt.getId())) loadApartments();
        }
    }

    private JButton createBackButton() {
        JButton btn = new JButton(" ← Quay lại");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(UIConstants.PRIMARY_COLOR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedButton extends JButton {
        private int arc;

        public RoundedButton(String text, int arc) {
            super(text);
            this.arc = arc;
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
