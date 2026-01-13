package view;

import dao.UserDAO;
import model.User;
import util.UIConstants;
import util.ModernButton;
import util.PermissionManager;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * User Management Panel
 * Only accessible by ADMIN
 */
public class UserManagementPanel extends JPanel {
    
    private UserDAO userDAO;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public UserManagementPanel() {
        this.userDAO = new UserDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadUsers();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Tài Khoản");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadUsers();
        });
        
        searchPanel.add(searchField);
        searchPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        String[] columns = {"ID", "Username", "Họ Tên", "Vai Trò", "Trạng Thái", "Đăng nhập cuối"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setFont(UIConstants.FONT_REGULAR);
        userTable.setRowHeight(45);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setShowGrid(true);
        userTable.setGridColor(UIConstants.BORDER_COLOR);
        
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editUser();
                }
            }
        });
        
        JTableHeader header = userTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Tài Khoản", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(170, 45));
        addButton.addActionListener(e -> addUser());
        
        ModernButton editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editUser());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteUser());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        
        for (User user : users) {
            String lastLogin = (user.getLastLogin() != null) ? 
                dateFormat.format(user.getLastLogin()) : "Chưa đăng nhập";
            
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRoleDisplayName(),
                user.isActive() ? "Hoạt động" : "Vô hiệu",
                lastLogin
            };
            tableModel.addRow(row);
        }
    }
    
    private void addUser() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        UserDialog dialog = new UserDialog(parentFrame);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User user = dialog.getUser();
            if (userDAO.insertUser(user)) {
                JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công!", 
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm tài khoản thất bại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần sửa!", 
                "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        User user = userDAO.getUserById(id);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        UserDialog dialog = new UserDialog(parentFrame, user);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User updatedUser = dialog.getUser();
            if (userDAO.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!", 
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thất bại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa!", 
                "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản đang đăng nhập!", 
                "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tài khoản '" + username + "'?",
            "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(id)) {
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công!", 
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thất bại!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}