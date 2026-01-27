package view;

import java.sql.Connection;
import java.sql.SQLException;
import util.UIConstants;
import util.PermissionManager;
import connection.Db_connection;

import util.PasswordUtil;
import dao.UserDAO;
import model.Building;
import model.Floor;
import model.User;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Main Dashboard với RBAC + User Management + User Dropdown
 * Hỗ trợ điều hướng công khai cho các Panel con: Tòa Nhà -> Tầng -> Căn Hộ
 */
public class MainDashboard extends JFrame {
    
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private PermissionManager permissionManager;
    
    // Menu buttons
    private JButton btnDashboard;
    private JButton btnBuildings; 
    private JButton btnResidents;
    private JButton btnContracts;
    private JButton btnServices;
    private JButton btnInvoices;
    private JButton btnReports;
    private JButton btnUsers;
    
    private JButton currentActiveButton = null;
    
    public MainDashboard() {
        this.permissionManager = PermissionManager.getInstance();
        
        initializeFrame();
        createSidebar();
        applyRoleBasedAccess();
        addUserDropdown();
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
        sidebarPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
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
        
        // Menu items
        btnDashboard = createMenuButton("Dashboard", "\u2637", true);
        btnBuildings = createMenuButton("Tòa Nhà", "\u25A3", false);
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
        sidebarPanel.add(btnResidents);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnContracts);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnServices);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnInvoices);
        sidebarPanel.add(Box.createVerticalStrut(3));
        sidebarPanel.add(btnReports);
        
        if (permissionManager.isAdmin()) {
            sidebarPanel.add(Box.createVerticalStrut(10));
            btnUsers = createMenuButton("Tài Khoản", "\u265F", false);
            sidebarPanel.add(btnUsers);
        }
        
        sidebarPanel.add(Box.createVerticalGlue());
        
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
        btnResidents.addActionListener(e -> showResidentsPanel());
        btnContracts.addActionListener(e -> showContractsPanel());
        btnServices.addActionListener(e -> showServicesPanel());
        btnInvoices.addActionListener(e -> showInvoicesPanel());
        btnReports.addActionListener(e -> showReportsPanel());
        
        if (btnUsers != null) {
            btnUsers.addActionListener(e -> showUsersPanel());
        }
    }

    // =============================================================
    // CÁC HÀM ĐIỀU HƯỚNG CÔNG KHAI (Đã đổi sang PUBLIC để sửa lỗi)
    // =============================================================

    /**
     * Hiển thị danh sách Tòa nhà
     * Dùng cho Sidebar và nút Quay lại từ trang Tầng
     */
    public void showBuildingsPanel() {
        if (!permissionManager.canAccess(PermissionManager.MODULE_BUILDINGS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Tòa Nhà");
            return;
        }
        BuildingManagementPanel panel = new BuildingManagementPanel(this::showFloorsOfBuilding);
        showPanel(panel, "Quản Lý Tòa Nhà", btnBuildings);
    }

    /**
     * Hiển thị danh sách Tầng của một tòa nhà cụ thể
     * Dùng khi click Card Tòa nhà hoặc nút Quay lại từ trang Căn hộ
     */
    public void showFloorsOfBuilding(Building building) {
        if (!permissionManager.canAccess(PermissionManager.MODULE_FLOORS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Tầng");
            return;
        }
        FloorManagementPanel floorPanel = new FloorManagementPanel(this::showApartmentsOfFloor);
        floorPanel.setBuilding(building);
        showPanel(floorPanel, "Quản Lý Tầng - " + building.getName(), btnBuildings);
    }

    /**
     * Hiển thị danh sách Căn hộ của một tầng
     */
    public void showApartmentsOfFloor(Floor floor) {
        if (!permissionManager.canAccess(PermissionManager.MODULE_APARTMENTS)) {
            permissionManager.showAccessDeniedMessage(this, "truy cập Căn Hộ");
            return;
        }
        ApartmentManagementPanel aptPanel = new ApartmentManagementPanel();
        aptPanel.setFloor(floor); 
        showPanel(aptPanel, "Quản Lý Căn Hộ - " + floor.getName(), btnBuildings);
    }

    // =============================================================
    // CÁC HÀM HỖ TRỢ UI KHÁC
    // =============================================================

    private void showPanel(JPanel panel, String title, JButton menuButton) {
        setActiveMenuButton(menuButton);
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void applyRoleBasedAccess() {
        btnBuildings.setVisible(permissionManager.canAccess(PermissionManager.MODULE_BUILDINGS));
        btnResidents.setVisible(permissionManager.canAccess(PermissionManager.MODULE_RESIDENTS));
        btnContracts.setVisible(permissionManager.canAccess(PermissionManager.MODULE_CONTRACTS));
        btnServices.setVisible(permissionManager.canAccess(PermissionManager.MODULE_SERVICES));
        btnInvoices.setVisible(permissionManager.canAccess(PermissionManager.MODULE_INVOICES));
        btnReports.setVisible(permissionManager.canAccess(PermissionManager.MODULE_REPORTS));
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    private void addUserDropdown() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // ✅ Container full width
        JPanel userContainer = new JPanel();
        userContainer.setLayout(new BoxLayout(userContainer, BoxLayout.Y_AXIS));
        userContainer.setBackground(UIConstants.SIDEBAR_COLOR);
        userContainer.setBorder(new EmptyBorder(10, 0, 20, 0));
        userContainer.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 80));
        userContainer.setAlignmentX(Component.LEFT_ALIGNMENT); // ← Added

        JButton userButton = new JButton();
        userButton.setLayout(new BorderLayout(12, 0));
        userButton.setBackground(new Color(45, 55, 72));
        userButton.setBorder(new EmptyBorder(12, 20, 12, 10));
        userButton.setFocusPainted(false);
        userButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        

        // ✅ CUSTOM PAINTED USER ICON (không dùng emoji)
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                // Draw user icon
                g2d.setColor(new Color(147, 197, 253)); // Light blue

                // Head circle
                g2d.fillOval(centerX - 8, centerY - 12, 16, 16);

                // Body arc
                g2d.fillArc(centerX - 12, centerY + 2, 24, 20, 0, -180);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(36, 36);
            }
        };
        avatarPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(currentUser.getRoleDisplayName());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(156, 163, 175));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(roleLabel);

        userButton.add(avatarPanel, BorderLayout.WEST);
        userButton.add(infoPanel, BorderLayout.CENTER);

        JLabel arrowLabel = new JLabel("▼");
        arrowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        arrowLabel.setForeground(new Color(156, 163, 175));
        userButton.add(arrowLabel, BorderLayout.EAST);

        userButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                userButton.setBackground(new Color(55, 65, 81)); 
            }
            public void mouseExited(MouseEvent e) { 
                userButton.setBackground(new Color(45, 55, 72)); 
            }
        });

        userButton.addActionListener(e -> showUserMenu(userButton));
        userContainer.add(userButton);
        sidebarPanel.add(userContainer);
    }

    private void showUserMenu(JButton userButton) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        JMenuItem headerItem = new JMenuItem(currentUser.getFullName());
        headerItem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerItem.setEnabled(false);
        headerItem.setBackground(new Color(249, 250, 251));
        popup.add(headerItem);
        popup.addSeparator();
        
        JMenuItem changePasswordItem = new JMenuItem("\ud83d\udd11 Đổi mật khẩu");
        changePasswordItem.setFont(UIConstants.FONT_REGULAR);
        changePasswordItem.addActionListener(e -> showChangePasswordDialog());
        popup.add(changePasswordItem);
        popup.addSeparator();
        
        JMenuItem logoutItem = new JMenuItem("\ud83d\udeaa Đăng xuất");
        logoutItem.setFont(UIConstants.FONT_REGULAR);
        logoutItem.setForeground(UIConstants.DANGER_COLOR);
        logoutItem.addActionListener(e -> performLogout());
        popup.add(logoutItem);
        
        popup.show(userButton, 0, userButton.getHeight());
    }

    private void showChangePasswordDialog() {
    // Create dialog panel
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
    
    int result = JOptionPane.showConfirmDialog(
        this, 
        panel, 
        "Đổi Mật Khẩu", 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE
    );
    
    if (result == JOptionPane.OK_OPTION) {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        UserDAO userDAO = new UserDAO();
        
        // Validate old password using BCrypt
        if (!userDAO.verifyCurrentPassword(currentUser.getId(), oldPassword)) {
            JOptionPane.showMessageDialog(
                this, 
                "Mật khẩu cũ không đúng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Validate new password length
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(
                this, 
                "Mật khẩu mới phải có ít nhất 6 ký tự!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Check password strength
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            String strengthDesc = PasswordUtil.getPasswordStrengthDescription(newPassword);
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mật khẩu không đủ mạnh!\n" + 
                "Đánh giá: " + strengthDesc + "\n\n" +
                "Bạn có muốn tiếp tục với mật khẩu này không?",
                "Cảnh Báo Bảo Mật",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                this, 
                "Mật khẩu xác nhận không khớp!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Check if new password is same as old
        if (oldPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(
                this, 
                "Mật khẩu mới phải khác mật khẩu cũ!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Change password (will be hashed by UserDAO)
        if (userDAO.changePassword(currentUser.getId(), newPassword)) {
            JOptionPane.showMessageDialog(
                this, 
                "Đổi mật khẩu thành công!\n" +
                "Vui lòng đăng nhập lại với mật khẩu mới.", 
                "Thành Công", 
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Auto logout after password change for security
            performLogout();
        } else {
            JOptionPane.showMessageDialog(
                this, 
                "Đổi mật khẩu thất bại!\n" +
                "Vui lòng thử lại sau.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE
            );
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
            public void mouseEntered(MouseEvent e) { if (btn != currentActiveButton) btn.setBackground(new Color(55, 65, 81)); }
            public void mouseExited(MouseEvent e) { if (btn != currentActiveButton) btn.setBackground(UIConstants.SIDEBAR_COLOR); }
        });
        return btn;
    }
    
    private void setActiveMenuButton(JButton activeButton) {
        JButton[] allButtons = {btnDashboard, btnBuildings, btnResidents, btnContracts, btnServices, btnInvoices, btnReports, btnUsers};
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
    
    private void createContentArea() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void showDashboardPanel() { showPanel(new DashboardPanel(), "Dashboard", btnDashboard); }
    private void showResidentsPanel() { showPanel(new ResidentManagementPanel(), "Quản Lý Cư Dân", btnResidents); }
    private void showContractsPanel() { showPanel(new ContractManagementPanel(), "Quản Lý Hợp Đồng", btnContracts); }
    private void showServicesPanel() { showPanel(new ServiceManagementPanel(), "Quản Lý Dịch Vụ", btnServices); }
    private void showInvoicesPanel() { showPanel(new InvoiceManagementPanel(), "Quản Lý Hóa Đơn", btnInvoices); }
    private void showReportsPanel() { showPanel(new ReportPanel(), "Báo Cáo", btnReports); }
    private void showUsersPanel() { showPanel(new UserManagementPanel(), "Quản Lý Tài Khoản", btnUsers); }
    
    private void testDatabaseConnection() {
    try (Connection conn = Db_connection.getConnection()) {
        if (conn != null && !conn.isClosed()) {
            System.out.println("✅ Database connection successful!");
        }
    } catch (SQLException e) {
        System.err.println("❌ Database connection failed!");
        e.printStackTrace();
    }
}
    private void performLogout() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác Nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            SwingUtilities.invokeLater(() -> { new LoginFrame(); dispose(); });
        }
    }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> { MainDashboard dashboard = new MainDashboard(); dashboard.setVisible(true); });
    }
}