package view;

import dao.*;
import model.*;
import util.UIConstants;
import util.MoneyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Compact Contract Form Dialog with Cascade Building → Floor → Apartment
 * Always creates new resident inline (no existing resident option)
 */
public class ContractFormDialog extends JDialog {
    
    private ContractDAO contractDAO;
    private ApartmentDAO apartmentDAO;
    private BuildingDAO buildingDAO;
    private FloorDAO floorDAO;
    private ResidentDAO residentDAO;
    private ServiceDAO serviceDAO;
    private ContractServiceDAO contractServiceDAO;
    
    private Contract contract;
    private boolean isEditMode;
    private boolean isConfirmed = false;
    
    // Form components - Contract Info
    private JTextField txtContractNumber;
    private JComboBox<String> cmbContractType;
    
    // Cascade filters: Building → Floor → Apartment
    private JComboBox<BuildingDisplay> cmbBuilding;
    private JComboBox<FloorDisplay> cmbFloor;
    private JComboBox<ApartmentDisplay> cmbApartment;
    
    // Resident fields (always inline)
    private JTextField txtResidentName;
    private JTextField txtResidentPhone;
    private JTextField txtResidentIdentityCard;
    private JComboBox<String> cmbResidentGender;
    private JSpinner spnResidentDob;
    private JTextField txtResidentEmail;
    
    // Contract dates
    private JSpinner spnSignedDate;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JCheckBox chkIndefinite;
    
    // Financial
    private JTextField txtDepositAmount;
    
    // Services
    private JPanel servicesPanel;
    private List<JCheckBox> serviceCheckboxes = new ArrayList<>();
    
    // Notes
    private JTextArea txtNotes;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private boolean isUpdatingCombos = false;
    
    public ContractFormDialog(JFrame parent, Contract contract) {
        super(parent, contract == null ? "Tạo Hợp Đồng Mới" : "Chỉnh Sửa Hợp Đồng", true);
        
        this.contractDAO = new ContractDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.buildingDAO = new BuildingDAO();
        this.floorDAO = new FloorDAO();
        this.residentDAO = new ResidentDAO();
        this.serviceDAO = new ServiceDAO();
        this.contractServiceDAO = new ContractServiceDAO();
        
        this.contract = contract != null ? contract : new Contract();
        this.isEditMode = contract != null && contract.getId() != null;
        
        initComponents();
        loadData();
        
        if (isEditMode) {
            loadContractData();
        }
        
        setSize(900, 820);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Header
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Contract Info
        mainPanel.add(createContractInfoSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Building → Floor → Apartment
        mainPanel.add(createLocationSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Resident Info (always shown)
        mainPanel.add(createResidentSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Dates
        mainPanel.add(createDatesSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Financial
        mainPanel.add(createFinancialSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Services
        mainPanel.add(createServicesSection());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // Notes
        mainPanel.add(createNotesSection());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel iconLabel = new JLabel(isEditMode ? "✏️" : "➕");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JLabel titleLabel = new JLabel(isEditMode ? "Chỉnh Sửa Hợp Đồng" : "Tạo Hợp Đồng Mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 33, 33));
        
        panel.add(iconLabel);
        panel.add(titleLabel);
        
        return panel;
    }
    
    private JPanel createContractInfoSection() {
        JPanel section = createSection("📋 Thông Tin Hợp Đồng");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Contract Number + Type
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Số hợp đồng:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtContractNumber = new JTextField();
        txtContractNumber.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContractNumber.setPreferredSize(new Dimension(0, 32));
        txtContractNumber.setEnabled(false);
        txtContractNumber.setBackground(new Color(245, 245, 245));
        txtContractNumber.setText(isEditMode ? "" : "Tự động tạo");
        section.add(txtContractNumber, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Loại hợp đồng:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        cmbContractType = new JComboBox<>(new String[]{"Thuê", "Sở hữu"});
        cmbContractType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbContractType.setPreferredSize(new Dimension(0, 32));
        section.add(cmbContractType, gbc);
        
        return section;
    }
    
    private JPanel createLocationSection() {
        JPanel section = createSection("🏢 Vị Trí Căn Hộ");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Building
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Tòa nhà:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        cmbBuilding = new JComboBox<>();
        cmbBuilding.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbBuilding.setPreferredSize(new Dimension(0, 32));
        cmbBuilding.addActionListener(e -> {
            if (!isUpdatingCombos) onBuildingChanged();
        });
        section.add(cmbBuilding, gbc);
        
        // Row 2: Floor + Apartment
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        section.add(createLabel("Tầng:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        cmbFloor = new JComboBox<>();
        cmbFloor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbFloor.setPreferredSize(new Dimension(0, 32));
        cmbFloor.addActionListener(e -> {
            if (!isUpdatingCombos) onFloorChanged();
        });
        section.add(cmbFloor, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Căn hộ:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        cmbApartment = new JComboBox<>();
        cmbApartment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbApartment.setPreferredSize(new Dimension(0, 32));
        section.add(cmbApartment, gbc);
        
        // ✅ THÊM: Custom renderers cho placeholders
        cmbBuilding.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Chọn tòa nhà --");
                    setForeground(new Color(158, 158, 158));
                }
                return this;
            }
        });
        
        cmbFloor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Chọn tầng --");
                    setForeground(new Color(158, 158, 158));
                }
                return this;
            }
        });
        
        cmbApartment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Chọn căn hộ --");
                    setForeground(new Color(158, 158, 158));
                }
                return this;
            }
        });
        
        return section;
    }
    
    private JPanel createResidentSection() {
        JPanel section = createSection("👤 Thông Tin Chủ Hộ Mới");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Full Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Họ và tên:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        txtResidentName = new JTextField();
        txtResidentName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtResidentName.setPreferredSize(new Dimension(0, 32));
        section.add(txtResidentName, gbc);
        
        // Row 2: Phone + Identity Card
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        section.add(createLabel("Số điện thoại:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtResidentPhone = new JTextField();
        txtResidentPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtResidentPhone.setPreferredSize(new Dimension(0, 32));
        section.add(txtResidentPhone, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("CCCD/CMND:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        txtResidentIdentityCard = new JTextField();
        txtResidentIdentityCard.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtResidentIdentityCard.setPreferredSize(new Dimension(0, 32));
        section.add(txtResidentIdentityCard, gbc);
        
        // Row 3: Gender + DOB
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        section.add(createLabel("Giới tính:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        cmbResidentGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cmbResidentGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbResidentGender.setPreferredSize(new Dimension(0, 32));
        section.add(cmbResidentGender, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Ngày sinh:", false), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        spnResidentDob = createDateSpinner();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -30);
        spnResidentDob.setValue(cal.getTime());
        section.add(spnResidentDob, gbc);
        
        // Row 4: Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        section.add(createLabel("Email:", false), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        txtResidentEmail = new JTextField();
        txtResidentEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtResidentEmail.setPreferredSize(new Dimension(0, 32));
        section.add(txtResidentEmail, gbc);
        
        return section;
    }
    
    private JPanel createDatesSection() {
        JPanel section = createSection("📅 Thời Hạn Hợp Đồng");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Signed Date + Start Date
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Ngày ký:", false), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnSignedDate = createDateSpinner();
        section.add(spnSignedDate, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Ngày bắt đầu:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        spnStartDate = createDateSpinner();
        section.add(spnStartDate, gbc);
        
        // Row 2: End Date + Indefinite checkbox
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        section.add(createLabel("Ngày kết thúc:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnEndDate = createDateSpinner();
        section.add(spnEndDate, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0; gbc.gridwidth = 2;
        chkIndefinite = new JCheckBox("Hợp đồng vô thời hạn");
        chkIndefinite.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkIndefinite.setBackground(Color.WHITE);
        chkIndefinite.addActionListener(e -> spnEndDate.setEnabled(!chkIndefinite.isSelected()));
        section.add(chkIndefinite, gbc);
        
        return section;
    }
    
    private JPanel createFinancialSection() {
        JPanel section = createSection("💰 Thông Tin Tài Chính");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Tiền cọc:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtDepositAmount = MoneyFormatter.createMoneyField(32);
        section.add(txtDepositAmount, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblCurrency = new JLabel("VNĐ");
        lblCurrency.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrency.setForeground(UIConstants.PRIMARY_COLOR);
        section.add(lblCurrency, gbc);
        
        return section;
    }
    
    private JPanel createServicesSection() {
        JPanel section = createSection("🔧 Dịch Vụ Áp Dụng");
        section.setLayout(new BorderLayout());
        
        servicesPanel = new JPanel();
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.Y_AXIS));
        servicesPanel.setBackground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(servicesPanel);
        scroll.setPreferredSize(new Dimension(0, 100));
        scroll.setBorder(null);
        
        section.add(scroll, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createNotesSection() {
        JPanel section = createSection("📝 Ghi Chú");
        section.setLayout(new BorderLayout());
        
        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(txtNotes);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        
        section.add(scroll, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        
        JButton btnCancel = createButton("Hủy", new Color(158, 158, 158));
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSave = createButton(isEditMode ? "💾 Lưu" : "✅ Tạo Hợp Đồng", UIConstants.PRIMARY_COLOR);
        btnSave.addActionListener(e -> saveContract());
        
        panel.add(btnCancel);
        panel.add(btnSave);
        
        return panel;
    }
    
    // ===== CASCADE FILTER LOGIC =====
    
    private void onBuildingChanged() {
        BuildingDisplay selected = (BuildingDisplay) cmbBuilding.getSelectedItem();
        if (selected == null) {
            cmbFloor.removeAllItems();
            cmbFloor.addItem(null);
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);
            return;
        }
        
        isUpdatingCombos = true;
        try {
            // Load floors for this building
            List<Floor> floors = floorDAO.getFloorsByBuildingId(selected.building.getId());
            
            cmbFloor.removeAllItems();
            cmbFloor.addItem(null);  // Item trống
            for (Floor floor : floors) {
                cmbFloor.addItem(new FloorDisplay(floor));
            }
            cmbFloor.setSelectedIndex(0);  // ← THÊM: Chọn item trống
            
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);
            cmbApartment.setSelectedIndex(0);  // ← THÊM: Chọn item trống
            
        } finally {
            isUpdatingCombos = false;
        }
    }
    
     private void onFloorChanged() {
        FloorDisplay selected = (FloorDisplay) cmbFloor.getSelectedItem();
        if (selected == null) {
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);
            return;
        }
        
        isUpdatingCombos = true;
        try {
            // Load apartments for this floor
            List<Apartment> apartments = apartmentDAO.getApartmentsByFloorId(selected.floor.getId());
            
            // Filter only AVAILABLE apartments for new contracts
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);  // Item trống
            for (Apartment apt : apartments) {
                if (isEditMode || "AVAILABLE".equals(apt.getStatus())) {
                    cmbApartment.addItem(new ApartmentDisplay(apt));
                }
            }
            cmbApartment.setSelectedIndex(0);  // ← THÊM: Chọn item trống
            
        } finally {
            isUpdatingCombos = false;
        }
    }

    
    // ===== DATA LOADING =====
    
    private void loadData() {
    // ✅ THÊM: Load buildings - Thêm item null cho mode tạo mới
    if (!isEditMode) {
        cmbBuilding.addItem(null);  // ← THÊM DÒNG NÀY
    }
    
    List<Building> buildings = buildingDAO.getAllBuildings();
    for (Building building : buildings) {
        cmbBuilding.addItem(new BuildingDisplay(building));
    }
    
    // ✅ THÊM: Chọn item trống (null) khi tạo mới
    if (!isEditMode) {
        cmbBuilding.setSelectedIndex(0);  // ← THÊM DÒNG NÀY
    }
    
    // Load services
    loadServices();
}
    
    private void loadServices() {
        List<Service> services = serviceDAO.getAllServices();
        serviceCheckboxes.clear();
        servicesPanel.removeAll();
        
        for (Service service : services) {
            JCheckBox cb = new JCheckBox(
                service.getServiceName() + " (" + 
                service.getUnitPrice().toPlainString() + " VNĐ/" + 
                service.getUnit() + ")"
            );
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cb.setBackground(Color.WHITE);
            cb.putClientProperty("service", service);
            
            // Auto-check mandatory services
            if (service.isMandatory()) {
                cb.setSelected(true);
                cb.setEnabled(false);
            }
            
            serviceCheckboxes.add(cb);
            servicesPanel.add(cb);
        }
        
        servicesPanel.revalidate();
        servicesPanel.repaint();
    }
    
    private void loadContractData() {
        // Load contract data for edit mode
        txtContractNumber.setText(contract.getContractNumber());
        
        String type = contract.getContractType();
        cmbContractType.setSelectedItem("RENTAL".equals(type) ? "Thuê" : "Sở hữu");
        
        // Load dates
        if (contract.getSignedDate() != null) {
            spnSignedDate.setValue(contract.getSignedDate());
        }
        spnStartDate.setValue(contract.getStartDate());
        
        if (contract.getEndDate() != null) {
            spnEndDate.setValue(contract.getEndDate());
        } else {
            chkIndefinite.setSelected(true);
            spnEndDate.setEnabled(false);
        }
        
        MoneyFormatter.setValue(txtDepositAmount, contract.getDepositAmount().longValue());
        
        if (contract.getNotes() != null) {
            txtNotes.setText(contract.getNotes());
        }
        
        // Load apartment and select building/floor
        Apartment apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
        if (apartment != null) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            if (floor != null) {
                selectBuildingAndFloor(floor.getBuildingId(), floor.getId(), apartment.getId());
            }
        }
        
        // Load services
        loadContractServices();
    }
    
    private void selectBuildingAndFloor(Long buildingId, Long floorId, Long apartmentId) {
        isUpdatingCombos = true;
        try {
            // Select building
            for (int i = 0; i < cmbBuilding.getItemCount(); i++) {
                if (cmbBuilding.getItemAt(i).building.getId().equals(buildingId)) {
                    cmbBuilding.setSelectedIndex(i);
                    break;
                }
            }
            
            // Select floor
            for (int i = 0; i < cmbFloor.getItemCount(); i++) {
                if (cmbFloor.getItemAt(i).floor.getId().equals(floorId)) {
                    cmbFloor.setSelectedIndex(i);
                    break;
                }
            }
            
            // Select apartment
            for (int i = 0; i < cmbApartment.getItemCount(); i++) {
                if (cmbApartment.getItemAt(i).apartment.getId().equals(apartmentId)) {
                    cmbApartment.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            isUpdatingCombos = false;
        }
    }
    
    private void loadContractServices() {
        List<ContractService> contractServices = contractServiceDAO.getServicesByContract(contract.getId());
        
        for (JCheckBox cb : serviceCheckboxes) {
            Service service = (Service) cb.getClientProperty("service");
            for (ContractService cs : contractServices) {
                if (cs.getServiceId().equals(service.getId())) {
                    cb.setSelected(true);
                    break;
                }
            }
        }
    }
    
    // ===== VALIDATION =====
    
    private boolean validateForm() {
        // Apartment
        if (cmbApartment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn căn hộ!\nChọn Tòa → Tầng → Căn hộ", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Resident validation
        if (txtResidentName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên chủ hộ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentName.requestFocus();
            return false;
        }
        
        String phone = txtResidentPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentPhone.requestFocus();
            return false;
        }
        if (!phone.matches("^[0-9]{10,11}$")) {
            JOptionPane.showMessageDialog(this, "SĐT không hợp lệ! (10-11 chữ số)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentPhone.requestFocus();
            return false;
        }
        
        String identityCard = txtResidentIdentityCard.getText().trim();
        if (identityCard.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD/CMND!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentIdentityCard.requestFocus();
            return false;
        }
        if (!identityCard.matches("^[0-9]{9}$") && !identityCard.matches("^[0-9]{12}$")) {
            JOptionPane.showMessageDialog(this, "CCCD/CMND không hợp lệ! (9 hoặc 12 số)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentIdentityCard.requestFocus();
            return false;
        }
        
        // Check duplicate CCCD (only for new contracts)
        if (!isEditMode && residentDAO.isIdentityCardExists(identityCard)) {
            JOptionPane.showMessageDialog(this, "CCCD/CMND này đã được sử dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentIdentityCard.requestFocus();
            return false;
        }
        
        // Email validation (optional)
        String email = txtResidentEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtResidentEmail.requestFocus();
            return false;
        }
        
        // Dates
        Date startDate = (Date) spnStartDate.getValue();
        if (!chkIndefinite.isSelected()) {
            Date endDate = (Date) spnEndDate.getValue();
            if (endDate.before(startDate)) {
                JOptionPane.showMessageDialog(this, 
                    "Ngày kết thúc phải sau ngày bắt đầu!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        // Deposit
        Long depositValue = MoneyFormatter.getValue(txtDepositAmount);
        if (depositValue == null || depositValue == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiền cọc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDepositAmount.requestFocus();
            return false;
        }
        
        if (depositValue < 0) {
            JOptionPane.showMessageDialog(this, "Tiền cọc phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDepositAmount.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // ===== SAVE =====
    
    private void saveContract() {
        if (!validateForm()) {
            return;
        }
        
        try {
            Long residentId;
            
            // ALWAYS CREATE NEW RESIDENT
            Resident newResident = new Resident();
            newResident.setFullName(txtResidentName.getText().trim());
            newResident.setPhone(txtResidentPhone.getText().trim());
            newResident.setIdentityCard(txtResidentIdentityCard.getText().trim());
            newResident.setGender((String) cmbResidentGender.getSelectedItem());
            newResident.setDob((Date) spnResidentDob.getValue());
            
            String email = txtResidentEmail.getText().trim();
            newResident.setEmail(email.isEmpty() ? null : email);
            
            // Save resident
            boolean residentCreated = residentDAO.insertResident(newResident);
            
            if (!residentCreated || newResident.getId() == null) {
                JOptionPane.showMessageDialog(this,
                    "Không thể tạo chủ hộ mới!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            residentId = newResident.getId();
            
            // Create contract
            ApartmentDisplay ad = (ApartmentDisplay) cmbApartment.getSelectedItem();
            
            contract.setApartmentId(ad.apartment.getId());
            contract.setResidentId(residentId);
            
            String typeDisplay = (String) cmbContractType.getSelectedItem();
            contract.setContractType("Thuê".equals(typeDisplay) ? "RENTAL" : "OWNERSHIP");
            
            contract.setSignedDate((Date) spnSignedDate.getValue());
            contract.setStartDate((Date) spnStartDate.getValue());
            
            if (chkIndefinite.isSelected()) {
                contract.setEndDate(null);
            } else {
                contract.setEndDate((Date) spnEndDate.getValue());
            }
            
            contract.setDepositAmount(BigDecimal.valueOf(MoneyFormatter.getValue(txtDepositAmount)));
            contract.setNotes(txtNotes.getText().trim());
            contract.setStatus("ACTIVE");
            
            // Generate contract number
            if (!isEditMode) {
                String contractNumber = contractDAO.generateContractNumber();
                contract.setContractNumber(contractNumber);
            }
            
            // Save contract
            boolean success;
            if (isEditMode) {
                success = contractDAO.updateContract(contract);
            } else {
                success = contractDAO.insertContract(contract);
            }
            
            if (success) {
                // Save services
                if (contract.getId() != null) {
                    saveContractServices();
                }
                
                isConfirmed = true;
                
                String message = isEditMode ? 
                    "Cập nhật hợp đồng thành công!" : 
                    "Tạo hợp đồng thành công!\nChủ hộ: " + newResident.getFullName();
                
                JOptionPane.showMessageDialog(this,
                    message,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật hợp đồng thất bại!" : "Tạo hợp đồng thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Có lỗi xảy ra: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
     private void saveContractServices() {
        try {
            // Collect selected service IDs
            List<Long> selectedServiceIds = new ArrayList<>();
            for (JCheckBox cb : serviceCheckboxes) {
                if (cb.isSelected()) {
                    Service service = (Service) cb.getClientProperty("service");
                    if (service != null && service.getId() != null) {
                        selectedServiceIds.add(service.getId());
                    }
                }
            }
            
            // If no services selected, skip
            if (selectedServiceIds.isEmpty()) {
                return;
            }
            
            // Get applied date (use contract start date)
            Date appliedDate = contract.getStartDate();
            if (appliedDate == null) {
                appliedDate = new Date(); // Fallback to today
            }
            
            // Insert services using batch method
            boolean success = contractServiceDAO.insertServicesForContract(
                contract.getId(), 
                selectedServiceIds, 
                appliedDate
            );
            
            if (!success) {
                System.err.println("Warning: Failed to save some contract services");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error saving contract services: " + e.getMessage());
        }
    }
    
    // ===== HELPER METHODS =====
    
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(66, 66, 66)
        ));
        return panel;
    }
    
    private JLabel createLabel(String text, boolean required) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (required) {
            label.setText("<html>" + text + " <font color='red'>*</font></html>");
        }
        return label;
    }
    
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setPreferredSize(new Dimension(0, 32));
        return spinner;
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ===== INNER CLASSES =====
    
    private class BuildingDisplay {
        Building building;
        BuildingDisplay(Building b) { this.building = b; }
        @Override
        public String toString() { return building.getName(); }
    }
    
    private class FloorDisplay {
        Floor floor;
        FloorDisplay(Floor f) { this.floor = f; }
        @Override
        public String toString() {
            return floor.getName() != null && !floor.getName().trim().isEmpty() ? 
                   floor.getName() : "Tầng " + floor.getFloorNumber();
        }
    }
    
    private class ApartmentDisplay {
        Apartment apartment;
        ApartmentDisplay(Apartment a) { this.apartment = a; }
        @Override
        public String toString() {
            return apartment.getRoomNumber() + " - " + apartment.getArea() + "m²";
        }
    }
    
    // ===== GETTERS =====
    
    public boolean isConfirmed() {
        return isConfirmed;
    }
    
    public Contract getContract() {
        return contract;
    }
}