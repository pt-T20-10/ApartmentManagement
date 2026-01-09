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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Contract Management Panel
 * Full CRUD operations for rental contracts
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
    
    // Form fields
    private JComboBox<ApartmentDisplay> apartmentCombo;
    private JComboBox<Resident> residentCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField monthlyRentField;
    private JTextField depositField;
    private JComboBox<String> statusCombo;
    
    private Contract selectedContract = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Inner class for apartment display
    private class ApartmentDisplay {
        Apartment apartment;
        String displayText;
        
        ApartmentDisplay(Apartment apartment, String displayText) {
            this.apartment = apartment;
            this.displayText = displayText;
        }
        
        @Override
        public String toString() {
            return displayText;
        }
    }
    
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
        createFormPanel();
        
        loadApartments();
        loadResidents();
        loadContracts();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[C]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Hop Dong");
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
        
        ModernButton searchButton = new ModernButton("[T] Tim Kiem", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchContracts());
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
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
        String[] columns = {"ID", "Can Ho", "Cu Dan", "Ngay BD", "Ngay KT", "Tien Thue", "Dat Coc", "Trang Thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        contractTable = new JTable(tableModel);
        contractTable.setFont(UIConstants.FONT_REGULAR);
        contractTable.setRowHeight(40);
        contractTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contractTable.setShowGrid(true);
        contractTable.setGridColor(UIConstants.BORDER_COLOR);
        contractTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedContract();
            }
        });
        
        // Table header
        JTableHeader header = contractTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER_COLOR));
        
        JScrollPane scrollPane = new JScrollPane(contractTable);
        scrollPane.setBorder(null);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        formContainer.setPreferredSize(new Dimension(400, 0));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Form title
        JLabel formTitle = new JLabel("Thong Tin Hop Dong");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Apartment field
        formPanel.add(createFieldLabel("Can Ho *"));
        apartmentCombo = new JComboBox<>();
        apartmentCombo.setFont(UIConstants.FONT_REGULAR);
        apartmentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        apartmentCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(apartmentCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Resident field
        formPanel.add(createFieldLabel("Cu Dan *"));
        residentCombo = new JComboBox<>();
        residentCombo.setFont(UIConstants.FONT_REGULAR);
        residentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        residentCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(residentCombo);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Start date field
        formPanel.add(createFieldLabel("Ngay Bat Dau (yyyy-MM-dd) *"));
        startDateField = createTextField();
        startDateField.setToolTipText("Vi du: 2026-01-01");
        formPanel.add(startDateField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // End date field
        formPanel.add(createFieldLabel("Ngay Ket Thuc (yyyy-MM-dd) *"));
        endDateField = createTextField();
        endDateField.setToolTipText("Vi du: 2027-01-01");
        formPanel.add(endDateField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Monthly rent field
        formPanel.add(createFieldLabel("Tien Thue/Thang *"));
        monthlyRentField = createTextField();
        formPanel.add(monthlyRentField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Deposit field
        formPanel.add(createFieldLabel("Tien Dat Coc *"));
        depositField = createTextField();
        formPanel.add(depositField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Status field
        formPanel.add(createFieldLabel("Trang Thai *"));
        statusCombo = new JComboBox<>(new String[]{"Active", "Expired", "Terminated"});
        statusCombo.setFont(UIConstants.FONT_REGULAR);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(statusCombo);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton addButton = new ModernButton("[+] Them", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addContract());
        
        ModernButton updateButton = new ModernButton("[E] Sua", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateContract());
        
        ModernButton deleteButton = new ModernButton("[X] Xoa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteContract());
        
        ModernButton clearButton = new ModernButton("[R] Lam Moi", UIConstants.TEXT_SECONDARY);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        formPanel.add(buttonPanel);
        
        formContainer.add(formPanel, BorderLayout.NORTH);
        add(formContainer, BorderLayout.EAST);
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_REGULAR);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }
    
    private void loadApartments() {
        DefaultComboBoxModel<ApartmentDisplay> model = new DefaultComboBoxModel<>();
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        
        for (Apartment apartment : apartments) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            if (floor != null) {
                Building building = buildingDAO.getBuildingById(floor.getBuildingId());
                String displayText = (building != null ? building.getName() : "N/A") + 
                                   " - Tang " + floor.getFloorNumber() + 
                                   " - " + apartment.getApartmentNumber();
                model.addElement(new ApartmentDisplay(apartment, displayText));
            }
        }
        
        apartmentCombo.setModel(model);
    }
    
    private void loadResidents() {
        DefaultComboBoxModel<Resident> model = new DefaultComboBoxModel<>();
        List<Resident> residents = residentDAO.getAllResidents();
        
        for (Resident resident : residents) {
            model.addElement(resident);
        }
        
        residentCombo.setModel(model);
        residentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Resident) {
                    Resident r = (Resident) value;
                    setText(r.getFullName() + " - " + r.getPhone());
                }
                return this;
            }
        });
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
    
    private void loadSelectedContract() {
        int selectedRow = contractTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedContract = contractDAO.getContractById(id);
            
            if (selectedContract != null) {
                // Select apartment
                for (int i = 0; i < apartmentCombo.getItemCount(); i++) {
                    ApartmentDisplay ad = apartmentCombo.getItemAt(i);
                    if (ad.apartment.getId().equals(selectedContract.getApartmentId())) {
                        apartmentCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Select resident
                for (int i = 0; i < residentCombo.getItemCount(); i++) {
                    Resident r = residentCombo.getItemAt(i);
                    if (r.getId().equals(selectedContract.getResidentId())) {
                        residentCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                startDateField.setText(selectedContract.getStartDate() != null ? 
                    dateFormat.format(selectedContract.getStartDate()) : "");
                endDateField.setText(selectedContract.getEndDate() != null ? 
                    dateFormat.format(selectedContract.getEndDate()) : "");
                monthlyRentField.setText(selectedContract.getMonthlyRent().toString());
                depositField.setText(selectedContract.getDeposit().toString());
                statusCombo.setSelectedItem(selectedContract.getStatus());
            }
        }
    }
    
    private void addContract() {
        if (!validateForm()) {
            return;
        }
        
        try {
            ApartmentDisplay selectedApt = (ApartmentDisplay) apartmentCombo.getSelectedItem();
            Resident selectedRes = (Resident) residentCombo.getSelectedItem();
            
            if (selectedApt == null || selectedRes == null) {
                JOptionPane.showMessageDialog(this, "Vui long chon can ho va cu dan!");
                return;
            }
            
            Contract contract = new Contract();
            contract.setApartmentId(selectedApt.apartment.getId());
            contract.setResidentId(selectedRes.getId());
            contract.setStartDate(dateFormat.parse(startDateField.getText().trim()));
            contract.setEndDate(dateFormat.parse(endDateField.getText().trim()));
            contract.setMonthlyRent(new BigDecimal(monthlyRentField.getText().trim()));
            contract.setDeposit(new BigDecimal(depositField.getText().trim()));
            contract.setStatus((String) statusCombo.getSelectedItem());
            
            if (contractDAO.insertContract(contract)) {
                JOptionPane.showMessageDialog(this, "Them hop dong thanh cong!");
                clearForm();
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, "Them hop dong that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateContract() {
        if (selectedContract == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon hop dong can sua!");
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        try {
            ApartmentDisplay selectedApt = (ApartmentDisplay) apartmentCombo.getSelectedItem();
            Resident selectedRes = (Resident) residentCombo.getSelectedItem();
            
            selectedContract.setApartmentId(selectedApt.apartment.getId());
            selectedContract.setResidentId(selectedRes.getId());
            selectedContract.setStartDate(dateFormat.parse(startDateField.getText().trim()));
            selectedContract.setEndDate(dateFormat.parse(endDateField.getText().trim()));
            selectedContract.setMonthlyRent(new BigDecimal(monthlyRentField.getText().trim()));
            selectedContract.setDeposit(new BigDecimal(depositField.getText().trim()));
            selectedContract.setStatus((String) statusCombo.getSelectedItem());
            
            if (contractDAO.updateContract(selectedContract)) {
                JOptionPane.showMessageDialog(this, "Cap nhat hop dong thanh cong!");
                clearForm();
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, "Cap nhat that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteContract() {
        if (selectedContract == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon hop dong can xoa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa hop dong nay?",
            "Xac Nhan Xoa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (contractDAO.deleteContract(selectedContract.getId())) {
                JOptionPane.showMessageDialog(this, "Xoa hop dong thanh cong!");
                clearForm();
                loadContracts();
            } else {
                JOptionPane.showMessageDialog(this, "Xoa that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
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
            if (contract.getStatus().toLowerCase().contains(keyword)) {
                // Add contract to table (same as loadContracts logic)
                Apartment apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
                String apartmentInfo = apartment != null ? apartment.getApartmentNumber() : "N/A";
                Resident resident = residentDAO.getResidentById(contract.getResidentId());
                String residentName = resident != null ? resident.getFullName() : "N/A";
                
                if (residentName.toLowerCase().contains(keyword) || 
                    apartmentInfo.toLowerCase().contains(keyword) ||
                    contract.getStatus().toLowerCase().contains(keyword)) {
                    
                    Object[] row = {
                        contract.getId(),
                        apartmentInfo,
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
        }
    }
    
    private void clearForm() {
        startDateField.setText("");
        endDateField.setText("");
        monthlyRentField.setText("");
        depositField.setText("");
        selectedContract = null;
        contractTable.clearSelection();
        if (apartmentCombo.getItemCount() > 0) apartmentCombo.setSelectedIndex(0);
        if (residentCombo.getItemCount() > 0) residentCombo.setSelectedIndex(0);
        if (statusCombo.getItemCount() > 0) statusCombo.setSelectedIndex(0);
    }
    
    private boolean validateForm() {
        if (startDateField.getText().trim().isEmpty() || endDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ngay bat dau va ket thuc!");
            return false;
        }
        
        try {
            Date start = dateFormat.parse(startDateField.getText().trim());
            Date end = dateFormat.parse(endDateField.getText().trim());
            
            if (end.before(start)) {
                JOptionPane.showMessageDialog(this, "Ngay ket thuc phai sau ngay bat dau!");
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Dinh dang ngay khong dung! (yyyy-MM-dd)");
            return false;
        }
        
        if (monthlyRentField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap tien thue!");
            return false;
        }
        
        try {
            BigDecimal rent = new BigDecimal(monthlyRentField.getText().trim());
            if (rent.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Tien thue phai lon hon 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tien thue khong hop le!");
            return false;
        }
        
        return true;
    }
}