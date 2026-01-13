package view;

import dao.UserDAO;
import model.User;
import util.SessionManager;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login Frame
 * User authentication interface
 */
public class LoginFrame extends JFrame {
    
    private UserDAO userDAO;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckbox;
    
    public LoginFrame() {
        this.userDAO = new UserDAO();
        
        initializeFrame();
        createLoginPanel();
        
        setVisible(true);
    }
    
    private void initializeFrame() {
        setTitle("Đăng Nhập - Hệ Thống Quản Lý Chung Cư");
        setSize(450, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Set background
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
    }
    
    private void createLoginPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        // Logo/Icon
        JLabel logoLabel = new JLabel("🏢", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        mainPanel.add(logoLabel, gbc);
        
        // Title
        JLabel titleLabel = new JLabel("QUẢN LÝ CHUNG CƯ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        mainPanel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Đăng nhập vào hệ thống", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        gbc.insets = new Insets(5, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);
        
        // Login form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        // Username field
        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(8));
        
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Focus effect
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
                    new EmptyBorder(9, 11, 9, 11)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
        
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(8));
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Focus effect
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
                    new EmptyBorder(9, 11, 9, 11)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
        
        // Enter key to login
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Remember me checkbox
        rememberCheckbox = new JCheckBox("Ghi nhớ đăng nhập");
        rememberCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberCheckbox.setBackground(Color.WHITE);
        rememberCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(rememberCheckbox);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Login button
        ModernButton loginButton = new ModernButton("Đăng Nhập", UIConstants.PRIMARY_COLOR);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());
        formPanel.add(loginButton);
        
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(formPanel, gbc);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel infoLabel = new JLabel("<html><center>Tài khoản mặc định:<br>" +
                                     "Admin: admin / admin123<br>" +
                                     "User: user / user123</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        infoPanel.add(infoLabel);
        
        gbc.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(infoPanel, gbc);
        
        add(mainPanel);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validation
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập!");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            passwordField.requestFocus();
            return;
        }
        
        // Show loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Authenticate
        User user = userDAO.authenticate(username, password);
        
        setCursor(Cursor.getDefaultCursor());
        
        if (user != null) {
            // Check if account is active
            if (!user.isActive()) {
                showError("Tài khoản đã bị vô hiệu hóa!");
                return;
            }
            
            // Set session
            SessionManager.getInstance().setCurrentUser(user);
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công!\nChào mừng " + user.getFullName(),
                "Thành Công",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open main dashboard
            SwingUtilities.invokeLater(() -> {
               MainDashboard dashboard = new MainDashboard();
                dashboard.setVisible(true); 
                 dispose();
            });
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng!");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Lỗi Đăng Nhập",
            JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}