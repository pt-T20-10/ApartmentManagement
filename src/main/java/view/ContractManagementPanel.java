package view;

import dao.ContractDAO;
import dao.ApartmentDAO;
import dao.ResidentDAO;
import dao.FloorDAO;
import dao.BuildingDAO;
import model.Contract;
import model.Apartment;
import model.Resident;
import model.Floor;
import model.Building;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Contract Management Panel
 * Full CRUD operations with popup dialog
 */
public class ContractManagementPanel extends JPanel {
    
    private ContractDAO contractDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private FloorDAO floorDAO;
    private BuildingDAO buildingDAO;
    
    private JTable contractTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public ContractManagementPanel() {
        this.contractDAO = new ContractDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.residentDAO = new ResidentDAO();
        this.floorDAO = new FloorDAO();
        this.buildingDAO = new BuildingDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createActionPanel();
        
        loadContracts();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("📄");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Quản Lý Hợp Đồng");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new JTextField(20);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.addActionListener(e -> searchContracts());
        
        ModernButton searchButton = new ModernButton("🔍 Tìm Kiếm", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchContracts());
        
        ModernButton refreshButton = new ModernButton("🔄 Làm Mới", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadContracts();
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        
        // Table model
        String[] columns = {"ID", "Căn Hộ", "Cư Dân", "Ngày BĐ", "Ngày KT", "Tiền Thuê", "Đặt Cọc", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        contractTable = new JTable(tableModel);
        contractTable.setFont(UIConstants.FONT_REGULAR);
        contractTable.setRowHeight(45);
        contractTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contractTable.setShowGrid(true);
        contractTable.setGridColor(UIConstants.BORDER_COLOR);
        
        // Double-click to edit
        contractTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editContract();
                }
            }
        });
        
        // Table header
        JTableHeader header = contractTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        // Column widths
        contractTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        contractTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        contractTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        contractTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        contractTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        contractTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        contractTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        contractTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(contractTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton addButton = new ModernButton("➕ Thêm Hợp Đồng", UIConstants.SUCCESS_COLOR);
        addButton.setPreferredSize(new Dimension(170, 45));
        addButton.addActionListener(e -> addContract());
        
        ModernButton editButton = new ModernButton("✏️ Sửa", UIConstants.WARNING_COLOR);
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editContract());
        
        ModernButton deleteButton = new ModernButton("🗑️ Xóa", UIConstants.DANGER_COLOR);
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteContract());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void loadContracts() {
        tableModel.setRowCount(0);
        List<Contract> contracts = contractDAO.getAllContracts();
        
        for (Contract contract : contracts) {
            Apartment apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
            String apartmentInfo = "N/A";
            if (apartment != null) {
                Floor floor = floorDAO.getFloorById(apartment.getFloorId());
                if (floor != null) {
                    Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                    apartmentInfo = (building != null ? building.getName() : "") + 
                                  " - " + apartment.getApartmentNumber();
                }
            }
            
            Resident resident = residentDAO.getResidentById(contract.getResidentId());
            String residentName = (resident != null) ? resident.getFullName() : "N/A";
            
            String startDateStr = (contract.getStartDate() != null) ? dateFormat.format(contract.getStartDate()) : "";
            String endDateStr = (contract.getEndDate() != null) ? dateFormat.format(contract.getEndDate()) : "";
            
            Object[] row = {
                contract.getId(),
                apartmentInfo,
                residentName,
                startDateStr,
                endDateStr,
                contract.getMonthlyRent(),
                contract.getDeposit(),
                contract.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addContract() {
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog
        ContractDialog dialog = new ContractDialog(parentFrame);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Contract contract = dialog.getContract();
            
            if (contractDAO.insertContract(contract)) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm hợp đồng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm hợp đồng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editContract() {
        int selectedRow = contractTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn hợp đồng cần sửa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Contract contract = contractDAO.getContractById(id);
        
        if (contract == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy hợp đồng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get parent frame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show dialog with existing contract
        ContractDialog dialog = new ContractDialog(parentFrame, contract);
        dialog.setVisible(true);
        
        // Check if confirmed
        if (dialog.isConfirmed()) {
            Contract updatedContract = dialog.getContract();
            
            if (contractDAO.updateContract(updatedContract)) {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật hợp đồng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Cập nhật hợp đồng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteContract() {
        int selectedRow = contractTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn hợp đồng cần xóa!", 
                "Cảnh Báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String apartmentInfo = (String) tableModel.getValueAt(selectedRow, 1);
        String residentName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hợp đồng:\n" + 
            "Căn hộ: " + apartmentInfo + "\n" +
            "Cư dân: " + residentName + "?",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (contractDAO.deleteContract(id)) {
                JOptionPane.showMessageDialog(this, 
                    "Xóa hợp đồng thành công!", 
                    "Thành Công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xóa hợp đồng thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchContracts() {
        String keyword = searchField.getText().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            loadContracts();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Contract> contracts = contractDAO.getAllContracts();
        
        for (Contract contract : contracts) {
            Apartment apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
            String apartmentInfo = apartment != null ? apartment.getApartmentNumber() : "";
            
            Resident resident = residentDAO.getResidentById(contract.getResidentId());
            String residentName = resident != null ? resident.getFullName() : "";
            
            if (residentName.toLowerCase().contains(keyword) || 
                apartmentInfo.toLowerCase().contains(keyword) ||
                contract.getStatus().toLowerCase().contains(keyword)) {
                
                String fullApartmentInfo = "N/A";
                if (apartment != null) {
                    Floor floor = floorDAO.getFloorById(apartment.getFloorId());
                    if (floor != null) {
                        Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                        fullApartmentInfo = (building != null ? building.getName() : "") + 
                                          " - " + apartment.getApartmentNumber();
                    }
                }
                
                Object[] row = {
                    contract.getId(),
                    fullApartmentInfo,
                    residentName,
                    dateFormat.format(contract.getStartDate()),
                    dateFormat.format(contract.getEndDate()),
                    contract.getMonthlyRent(),
                    contract.getDeposit(),
                    contract.getStatus()
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy hợp đồng nào!", 
                "Thông Báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}