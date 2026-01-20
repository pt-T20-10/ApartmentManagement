package view;

import dao.BuildingDAO;
import model.Building;
import util.UIConstants;
import dao.BuildingDAO.BuildingStats;
import util.BuildingContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BuildingManagementPanel extends JPanel {

    private BuildingDAO buildingDAO;
    private JPanel cardsContainer;
    private JButton btnAdd;
    private Consumer<Building> onBuildingSelect; 

    public BuildingManagementPanel(Consumer<Building> onBuildingSelect) {
        this.onBuildingSelect = building -> {
            BuildingContext.getInstance().setCurrentBuilding(building);
            if (onBuildingSelect != null) {
                onBuildingSelect.accept(building);
            }
        };
        this.buildingDAO = new BuildingDAO();
        initUI();
        loadBuildings();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Quản Lý Tòa Nhà");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        btnAdd = new RoundedButton("Thêm Tòa Nhà", 15);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(UIConstants.PRIMARY_COLOR);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.addActionListener(e -> showAddDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- CONTENT ---
        cardsContainer = new JPanel(new GridLayout(0, 2, 25, 25)); 
        cardsContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BACKGROUND_COLOR);
        wrapper.add(cardsContainer, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static class RoundedButton extends JButton {
        private int arc;
        public RoundedButton(String text, int arc) {
            super(text);
            this.arc = arc;
            setContentAreaFilled(false);
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

    private void loadBuildings() {
        cardsContainer.removeAll();
        List<Building> list = buildingDAO.getAllBuildings();
        
        for (Building b : list) {
            BuildingStats stats = buildingDAO.getBuildingStatistics(b.getId());
            cardsContainer.add(new BuildingCard(
                b, 
                stats, 
                onBuildingSelect, // Card tự xử lý chặn click
                this::editBuilding, 
                this::deleteBuilding
            ));
        }
        
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void showAddDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BuildingDialog dialog = new BuildingDialog(parent, new Building());
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (buildingDAO.insertBuilding(dialog.getBuilding())) {
                JOptionPane.showMessageDialog(this, "Thêm tòa nhà thành công!");
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        }
    }

    private void editBuilding(Building b) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        BuildingDialog dialog = new BuildingDialog(parent, b);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (buildingDAO.updateBuilding(dialog.getBuilding())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        }
    }

    private void deleteBuilding(Building b) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa tòa nhà " + b.getName() + "?\nCác tầng và căn hộ bên trong cũng sẽ bị ảnh hưởng.", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (buildingDAO.deleteBuilding(b.getId())) {
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }
}