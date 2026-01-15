package view;

import dao.BuildingDAO;
import model.Building;
import util.UIConstants;
import dao.BuildingDAO.BuildingStats; // [QUAN TRỌNG] Import class này

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BuildingManagementPanel extends JPanel {

    private BuildingDAO buildingDAO;
    private JPanel cardsContainer;
    private JButton btnAdd;
    
    // Biến lưu hành động chuyển trang
    private Consumer<Building> onBuildingSelect; 

    public BuildingManagementPanel(Consumer<Building> onBuildingSelect) {
        this.onBuildingSelect = onBuildingSelect;
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
    
    // [SỬA] Sử dụng RoundedButton để bo góc nút
    btnAdd = new RoundedButton("Thêm Tòa Nhà", 15); // Bo góc 15px
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
    // [SỬA] GridLayout(0, 2) để luôn hiển thị 2 cột
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

// Thêm Inner Class này vào cuối file BuildingManagementPanel nếu chưa có
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
            // [FIX LỖI TẠI ĐÂY]: Lấy thống kê trước khi tạo Card
            BuildingStats stats = buildingDAO.getBuildingStatistics(b.getId());
            
            // Truyền đủ 5 tham số: Building, Stats, onSelect, onEdit, onDelete
            cardsContainer.add(new BuildingCard(
                b, 
                stats, 
                onBuildingSelect, 
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