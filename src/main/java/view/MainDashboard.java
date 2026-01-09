package view;

import util.UIConstants;
import connection.Db_connection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Main Dashboard - FIXED with proper icon rendering
 */
public class MainDashboard extends JFrame {
    
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    
    // Menu buttons
    private JButton btnDashboard;
    private JButton btnBuildings;
    private JButton btnFloors;
    private JButton btnApartments;
    private JButton btnResidents;
    private JButton btnContracts;
    private JButton btnServices;
    private JButton btnInvoices;
    private JButton btnReports;
    
    private JButton currentActiveButton = null;
    
    public MainDashboard() {
        initializeFrame();
        createSidebar();
        createContentArea();
        testDatabaseConnection();
        
        // Show dashboard by default
        showDashboardPanel();
    }
    
    private void initializeFrame() {
        setTitle("Hệ Thống Quản Lý Chung Cư");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UIConstants.SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Logo/Title area
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(UIConstants.SIDEBAR_COLOR);
        logoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 100));
        logoPanel.setBorder(new EmptyBorder(0, 20, 30, 20));
        
        JLabel logoLabel = new JLabel("\u25A0 QUẢN LÝ");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("CHUNG CƯ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subtitleLabel.setForeground(UIConstants.PRIMARY_LIGHT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(5));
        logoPanel.add(subtitleLabel);
        
        sidebarPanel.add(logoPanel);
        sidebarPanel.add(createSeparator());
        
        // Menu items with Unicode icons
        btnDashboard = createMenuButton("Dashboard", "\u2637", true);
        btnBuildings = createMenuButton("Tòa Nhà", "\u25A3", false);
        btnFloors = createMenuButton("Tầng", "\u2261", false);
        btnApartments = createMenuButton("Căn Hộ", "\u2302", false);
        btnResidents = createMenuButton("Cư Dân", "\u265B", false);
        btnContracts = createMenuButton("Hợp Đồng", "\u2709", false);
        btnServices = createMenuButton("Dịch Vụ", "\u26A1", false);
        btnInvoices = createMenuButton("Hóa Đơn", "\u263C", false);
        btnReports = createMenuButton("Báo Cáo", "\u2630", false);
        
        currentActiveButton = btnDashboard;
        
        sidebarPanel.add(btnDashboard);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnBuildings);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnFloors);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnApartments);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnResidents);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnContracts);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnServices);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnInvoices);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnReports);
        
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(createSeparator());
        
        // Footer info
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(UIConstants.FONT_SMALL);
        versionLabel.setForeground(UIConstants.TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        sidebarPanel.add(versionLabel);
        
        add(sidebarPanel, BorderLayout.WEST);
        
        // Add action listeners
        btnDashboard.addActionListener(e -> showDashboardPanel());
        btnBuildings.addActionListener(e -> showBuildingsPanel());
        btnFloors.addActionListener(e -> showFloorsPanel());
        btnApartments.addActionListener(e -> showApartmentsPanel());
        btnResidents.addActionListener(e -> showResidentsPanel());
        btnContracts.addActionListener(e -> showContractsPanel());
        btnServices.addActionListener(e -> showServicesPanel());
        btnInvoices.addActionListener(e -> showInvoicesPanel());
        btnReports.addActionListener(e -> showReportsPanel());
    }
    
    /**
     * Create menu button with Unicode icon - FIXED
     */
    private JButton createMenuButton(String text, String iconChar, boolean isActive) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(10, 0));
        btn.setBorder(new EmptyBorder(0, 15, 0, 15));
        
        // Icon panel with fixed size
        JPanel iconContainer = new JPanel(new GridBagLayout());
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(30, 48));
        
        JLabel iconLabel = new JLabel(iconChar);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        iconLabel.setForeground(isActive ? Color.WHITE : UIConstants.TEXT_SECONDARY);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconContainer.add(iconLabel);
        
        // Text label
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(UIConstants.FONT_MENU);
        textLabel.setForeground(isActive ? Color.WHITE : UIConstants.TEXT_SECONDARY);
        textLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        
        btn.add(iconContainer, BorderLayout.WEST);
        btn.add(textLabel, BorderLayout.CENTER);
        
        // Button properties
        btn.setBackground(isActive ? UIConstants.SIDEBAR_HOVER : UIConstants.SIDEBAR_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        
        // Fixed size
        Dimension buttonSize = new Dimension(UIConstants.SIDEBAR_WIDTH, 48);
        btn.setMinimumSize(buttonSize);
        btn.setMaximumSize(buttonSize);
        btn.setPreferredSize(buttonSize);
        
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Store components
        btn.putClientProperty("iconLabel", iconLabel);
        btn.putClientProperty("textLabel", textLabel);
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(new Color(55, 65, 81));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(UIConstants.SIDEBAR_COLOR);
                }
            }
        });
        
        return btn;
    }
    
    private void setActiveMenuButton(JButton activeButton) {
        JButton[] allButtons = {btnDashboard, btnBuildings, btnFloors, btnApartments, 
                                btnResidents, btnContracts, btnServices, btnInvoices, btnReports};
        
        for (JButton btn : allButtons) {
            btn.setBackground(UIConstants.SIDEBAR_COLOR);
            
            JLabel iconLabel = (JLabel) btn.getClientProperty("iconLabel");
            JLabel textLabel = (JLabel) btn.getClientProperty("textLabel");
            
            if (iconLabel != null) iconLabel.setForeground(UIConstants.TEXT_SECONDARY);
            if (textLabel != null) textLabel.setForeground(UIConstants.TEXT_SECONDARY);
        }
        
        currentActiveButton = activeButton;
        activeButton.setBackground(UIConstants.SIDEBAR_HOVER);
        
        JLabel iconLabel = (JLabel) activeButton.getClientProperty("iconLabel");
        JLabel textLabel = (JLabel) activeButton.getClientProperty("textLabel");
        
        if (iconLabel != null) iconLabel.setForeground(Color.WHITE);
        if (textLabel != null) textLabel.setForeground(Color.WHITE);
        
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
    
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(75, 85, 99));
        separator.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 1));
        separator.setBorder(new EmptyBorder(10, 20, 10, 20));
        return separator;
    }
    
    private void createContentArea() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void showPanel(JPanel panel, String title, JButton menuButton) {
        setActiveMenuButton(menuButton);
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showDashboardPanel() {
        showPanel(new DashboardPanel(), "Dashboard", btnDashboard);
    }
    
    private void showBuildingsPanel() {
        showPanel(new BuildingManagementPanel(), "Quản Lý Tòa Nhà", btnBuildings);
    }
    
    private void showFloorsPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Tầng", "\u2261"), "Quản Lý Tầng", btnFloors);
    }
    
    private void showApartmentsPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Căn Hộ", "\u2302"), "Quản Lý Căn Hộ", btnApartments);
    }
    
    private void showResidentsPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Cư Dân", "\u265B"), "Quản Lý Cư Dân", btnResidents);
    }
    
    private void showContractsPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Hợp Đồng", "\u2709"), "Quản Lý Hợp Đồng", btnContracts);
    }
    
    private void showServicesPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Dịch Vụ", "\u26A1"), "Quản Lý Dịch Vụ", btnServices);
    }
    
    private void showInvoicesPanel() {
        showPanel(createPlaceholderPanel("Quản Lý Hóa Đơn", "\u263C"), "Quản Lý Hóa Đơn", btnInvoices);
    }
    
    private void showReportsPanel() {
        showPanel(createPlaceholderPanel("Báo Cáo Thống Kê", "\u2630"), "Báo Cáo", btnReports);
    }
    
    private JPanel createPlaceholderPanel(String title, String icon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(50, 50, 50, 50)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Chức năng đang được phát triển");
        subtitleLabel.setFont(UIConstants.FONT_REGULAR);
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitleLabel);
        
        panel.add(card);
        return panel;
    }
    
   private void testDatabaseConnection() {
    try {
        if (Db_connection.getConnection() != null) {
            System.out.println("✅ Database connection successful!");
        } else {
            JOptionPane.showMessageDialog(this,
                "Không thể kết nối đến cơ sở dữ liệu!\nVui lòng kiểm tra MySQL server.",
                "Lỗi Kết Nối",
                JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Không thể kết nối đến cơ sở dữ liệu!\nVui lòng kiểm tra MySQL server.",
            "Lỗi Kết Nối",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}