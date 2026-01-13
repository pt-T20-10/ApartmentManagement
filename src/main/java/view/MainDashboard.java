package view;

import util.UIConstants;
import util.PermissionManager;
import connection.Db_connection;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import util.SessionManager;
import model.User;
import util.ModernButton;

/**
 * Main Dashboard with RBAC + User Management + User Dropdown
 */
public class MainDashboard extends JFrame {
    
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private PermissionManager permissionManager;
    
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
    private JButton btnUsers; // NEW: User Management tab
    
    private JButton currentActiveButton = null;
    
    public MainDashboard() {
        this.permissionManager = PermissionManager.getInstance();
        
        initializeFrame();
        createSidebar();
        applyRoleBasedAccess();
        addUserDropdown(); // NEW: User dropdown instead of static info
        createContentArea();
        testDatabaseConnection();
        
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
        
        // Logo/Title
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
//        sidebarPanel.add(createSeparator());
        
        // Menu items
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
        
        // NEW: User Management tab (only for ADMIN)
        if (permissionManager.isAdmin()) {
            sidebarPanel.add(Box.createVerticalStrut(10));
//            sidebarPanel.add(createSeparator());
            btnUsers = createMenuButton("Tài Khoản", "\u265F", false);
            sidebarPanel.add(btnUsers);
        }
        
        sidebarPanel.add(Box.createVerticalGlue());
//        sidebarPanel.add(createSeparator());
        
        // Footer
        JLabel versionLabel = new JLabel("Version 1.0.0 RBAC");
        versionLabel.setFont(UIConstants.FONT_SMALL);
        versionLabel.setForeground(UIConstants.TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        sidebarPanel.add(versionLabel);
        
        add(sidebarPanel, BorderLayout.WEST);
        
        // Action listeners
        btnDashboard.addActionListener(e -> showDashboardPanel());
        btnBuildings.addActionListener(e -> showBuildingsPanel());
        btnFloors.addActionListener(e -> showFloorsPanel());
        btnApartments.addActionListener(e -> showApartmentsPanel());
        btnResidents.addActionListener(e -> showResidentsPanel());
        btnContracts.addActionListener(e -> showContractsPanel());
        btnServices.addActionListener(e -> showServicesPanel());
        btnInvoices.addActionListener(e -> showInvoicesPanel());
        btnReports.addActionListener(e -> showReportsPanel());
        
        if (btnUsers != null) {
            btnUsers.addActionListener(e -> showUsersPanel());
        }
    }
    
    private void applyRoleBasedAccess() {
        btnBuildings.setVisible(permissionManager.canAccess(PermissionManager.MODULE_BUILDINGS));
        btnFloors.setVisible(permissionManager.canAccess(PermissionManager.MODULE_FLOORS));
        btnApartments.setVisible(permissionManager.canAccess(PermissionManager.MODULE_APARTMENTS));
        btnResidents.setVisible(permissionManager.canAccess(PermissionManager.MODULE_RESIDENTS));
        btnContracts.setVisible(permissionManager.canAccess(PermissionManager.MODULE_CONTRACTS));
        btnServices.setVisible(permissionManager.canAccess(PermissionManager.MODULE_SERVICES));
        btnInvoices.setVisible(permissionManager.canAccess(PermissionManager.MODULE_INVOICES));
        btnReports.setVisible(permissionManager.canAccess(PermissionManager.MODULE_REPORTS));
        
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }
    
    /**
     * NEW: Add user dropdown (replaces old static user info)
     */
    private void addUserDropdown() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        JPanel userContainer = new JPanel();
        userContainer.setLayout(new BoxLayout(userContainer, BoxLayout.Y_AXIS));
        userContainer.setBackground(UIConstants.SIDEBAR_COLOR);
        userContainer.setBorder(new EmptyBorder(10, 15, 10, 15));
        userContainer.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 70));
        
        // User button (clickable dropdown)
        JButton userButton = new JButton();
        userButton.setLayout(new BorderLayout(10, 0));
        userButton.setBackground(new Color(45, 55, 72));
        userButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        userButton.setFocusPainted(false);
        userButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Avatar
        JLabel avatar = new JLabel("👤");
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        
        // User info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(Color.WHITE);
        
        JLabel roleLabel = new JLabel(currentUser.getRoleDisplayName());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        infoPanel.add(nameLabel);
        infoPanel.add(roleLabel);
        
        userButton.add(avatar, BorderLayout.WEST);
        userButton.add(infoPanel, BorderLayout.CENTER);
        
        // Dropdown arrow
        JLabel arrowLabel = new JLabel("▼");
        arrowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        arrowLabel.setForeground(UIConstants.TEXT_SECONDARY);
        userButton.add(arrowLabel, BorderLayout.EAST);
        
        // Hover effect
        userButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                userButton.setBackground(new Color(55, 65, 81));
            }
            public void mouseExited(MouseEvent e) {
                userButton.setBackground(new Color(45, 55, 72));
            }
        });
        
        // Click to show dropdown
        userButton.addActionListener(e -> showUserMenu(userButton));
        
        userContainer.add(userButton);
        sidebarPanel.add(userContainer);
    }
    
    /**
     * NEW: Show user dropdown menu
     */
    private void showUserMenu(JButton userButton) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Header (non-clickable)
        JMenuItem headerItem = new JMenuItem(currentUser.getFullName());
        headerItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerItem.setEnabled(false);
        headerItem.setBackground(new Color(249, 250, 251));
        popup.add(headerItem);
        
        popup.addSeparator();
        
        // Change password
        JMenuItem changePasswordItem = new JMenuItem("🔑 Đổi mật khẩu");
        changePasswordItem.setFont(UIConstants.FONT_REGULAR);
        changePasswordItem.addActionListener(e -> showChangePasswordDialog());
        popup.add(changePasswordItem);
        
        popup.addSeparator();
        
        // Logout
        JMenuItem logoutItem = new JMenuItem("🚪 Đăng xuất");
        logoutItem.setFont(UIConstants.FONT_REGULAR);
        logoutItem.setForeground(UIConstants.DANGER_COLOR);
        logoutItem.addActionListener(e -> performLogout());
        popup.add(logoutItem);
        
        popup.show(userButton, 0, userButton.getHeight());
    }
    
    /**
     * NEW: Show change password dialog
     */
    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPasswordField oldPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        
        panel.add(new JLabel("Mật khẩu cũ:"));
        panel.add(oldPasswordField);
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Xác nhận:"));
        panel.add(confirmPasswordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Đổi Mật Khẩu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            User currentUser = SessionManager.getInstance().getCurrentUser();
            
            // Validation
            if (!currentUser.getPassword().equals(oldPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu cũ không đúng!");
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }
            
            // Change password
            UserDAO userDAO = new UserDAO();
            if (userDAO.changePassword(currentUser.getId(), newPassword)) {
                currentUser.setPassword(newPassword);
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!");
            }
        }
    }
    
    private JButton createMenuButton(String text, String iconChar, boolean isActive) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(10, 0));
        btn.setBorder(new EmptyBorder(0, 15, 0, 15));
        
        JPanel iconContainer = new JPanel(new GridBagLayout());
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(30, 48));
        
        JLabel iconLabel = new JLabel(iconChar);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        iconLabel.setForeground(isActive ? Color.WHITE : UIConstants.TEXT_SECONDARY);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconContainer.add(iconLabel);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(UIConstants.FONT_MENU);
        textLabel.setForeground(isActive ? Color.WHITE : UIConstants.TEXT_SECONDARY);
        textLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        
        btn.add(iconContainer, BorderLayout.WEST);
        btn.add(textLabel, BorderLayout.CENTER);
        
        btn.setBackground(isActive ? UIConstants.SIDEBAR_HOVER : UIConstants.SIDEBAR_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        
        Dimension buttonSize = new Dimension(UIConstants.SIDEBAR_WIDTH, 48);
        btn.setMinimumSize(buttonSize);
        btn.setMaximumSize(buttonSize);
        btn.setPreferredSize(buttonSize);
        
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("iconLabel", iconLabel);
        btn.putClientProperty("textLabel", textLabel);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(new Color(55, 65, 81));
                }
            }
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
                                btnResidents, btnContracts, btnServices, btnInvoices, 
                                btnReports, btnUsers};
        
        for (JButton btn : allButtons) {
            if (btn == null) continue;
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
    
//    private JSeparator createSeparator() {
//        JSeparator separator = new JSeparator();
//        separator.setForeground(new Color(75, 85, 99));
//        separator.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 1));
//        separator.setBorder(new EmptyBorder(10, 20, 10, 20));
//        return separator;
//    }
    
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
        if (!permissionManager.canAccess(PermissionManager.MODULE_BUILDINGS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Tòa Nhà");
            return;
        }
        showPanel(new BuildingManagementPanel(), "Quản Lý Tòa Nhà", btnBuildings);
    }
    
    private void showFloorsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_FLOORS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Tầng");
            return;
        }
        showPanel(new FloorManagementPanel(), "Quản Lý Tầng", btnFloors);
    }
    
    private void showApartmentsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_APARTMENTS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Căn Hộ");
            return;
        }
        showPanel(new ApartmentManagementPanel(), "Quản Lý Căn Hộ", btnApartments);
    }
    
    private void showResidentsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_RESIDENTS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Cư Dân");
            return;
        }
        showPanel(new ResidentManagementPanel(), "Quản Lý Cư Dân", btnResidents);
    }
    
    private void showContractsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_CONTRACTS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Hợp Đồng");
            return;
        }
        showPanel(new ContractManagementPanel(), "Quản Lý Hợp Đồng", btnContracts);
    }
    
    private void showServicesPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_SERVICES)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Dịch Vụ");
            return;
        }
        showPanel(new ServiceManagementPanel(), "Quản Lý Dịch Vụ", btnServices);
    }
    
    private void showInvoicesPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_INVOICES)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Hóa Đơn");
            return;
        }
        showPanel(new InvoiceManagementPanel(), "Quản Lý Hóa Đơn", btnInvoices);
    }
    
    private void showReportsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_REPORTS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Báo Cáo");
            return;
        }
        showPanel(new ReportPanel(), "Báo Cáo", btnReports);
    }
    
    /**
     * NEW: Show User Management panel (ADMIN only)
     */
    private void showUsersPanel() {
        if (!permissionManager.isAdmin()) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Quản Lý Tài Khoản");
            return;
        }
        showPanel(new UserManagementPanel(), "Quản Lý Tài Khoản", btnUsers);
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
    
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác Nhận Đăng Xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            
            SwingUtilities.invokeLater(() -> {
                new LoginFrame();
                dispose();
            });
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