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

/**
 * Contract Form Dialog with DYNAMIC UI based on contract type
 * 
 * RENTAL (Thuê):
 * - Price field: "Tiền thuê/tháng"
 * - Dates: Ngày ký + Ngày bắt đầu + Ngày kết thúc
 * 
 * OWNERSHIP (Sở hữu):
 * - Price field: "Giá mua"
 * - Dates: CHỈ Ngày ký (no start/end dates)
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
    
    // Cascade filters
    private JComboBox<BuildingDisplay> cmbBuilding;
    private JComboBox<FloorDisplay> cmbFloor;
    private JComboBox<ApartmentDisplay> cmbApartment;
    
    // Resident fields
    private JTextField txtResidentName;
    private JTextField txtResidentPhone;
    private JTextField txtResidentIdentityCard;
    private JComboBox<String> cmbResidentGender;
    private JSpinner spnResidentDob;
    private JTextField txtResidentEmail;
    
    // Contract dates (visibility depends on type)
    private JLabel lblSignedDate;
    private JSpinner spnSignedDate;
    private JLabel lblStartDate;
    private JSpinner spnStartDate;
    private JLabel lblEndDate;
    private JSpinner spnEndDate;
    private JCheckBox chkIndefinite;
    private JPanel datesSection; // Reference to rebuild
    
    // Financial (dynamic label)
    private JLabel lblPriceField;
    private JTextField txtPriceAmount;
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
        
        setSize(900, 870);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createContractInfoSection());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createLocationSection());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createResidentSection());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createDatesSection()); // ✅ Dynamic dates
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createFinancialSection()); // ✅ Dynamic price
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createServicesSection());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(createNotesSection());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
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
        
        // ✅ LISTENER: Update UI when contract type changes
        cmbContractType.addActionListener(e -> onContractTypeChanged());
        
        section.add(cmbContractType, gbc);
        
        return section;
    }
    
    // ✅ NEW: Handle contract type change
    private void onContractTypeChanged() {
        String selectedType = (String) cmbContractType.getSelectedItem();
        boolean isRental = "Thuê".equals(selectedType);
        
        // Update price label
        if (lblPriceField != null) {
            lblPriceField.setText(isRental ? 
                "<html>Tiền thuê/tháng: <font color='red'>*</font></html>" : 
                "<html>Giá mua: <font color='red'>*</font></html>");
        }
        
        // Update dates visibility
        if (lblStartDate != null && lblEndDate != null) {
            lblStartDate.setVisible(isRental);
            spnStartDate.setVisible(isRental);
            lblEndDate.setVisible(isRental);
            spnEndDate.setVisible(isRental);
            if (chkIndefinite != null) {
                chkIndefinite.setVisible(isRental);
            }
        }
        
        // Force repaint
        if (datesSection != null) {
            datesSection.revalidate();
            datesSection.repaint();
        }
    }
    
    private JPanel createLocationSection() {
        JPanel section = createSection("🏢 Vị Trí Căn Hộ");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Tòa nhà:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        cmbBuilding = new JComboBox<>();
        cmbBuilding.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbBuilding.setPreferredSize(new Dimension(0, 32));
        cmbBuilding.addActionListener(e -> { if (!isUpdatingCombos) onBuildingChanged(); });
        section.add(cmbBuilding, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        section.add(createLabel("Tầng:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        cmbFloor = new JComboBox<>();
        cmbFloor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbFloor.setPreferredSize(new Dimension(0, 32));
        cmbFloor.addActionListener(e -> { if (!isUpdatingCombos) onFloorChanged(); });
        section.add(cmbFloor, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Căn hộ:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        cmbApartment = new JComboBox<>();
        cmbApartment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbApartment.setPreferredSize(new Dimension(0, 32));
        section.add(cmbApartment, gbc);
        
        // Renderers
        cmbBuilding.setRenderer(new PlaceholderRenderer("-- Chọn tòa nhà --"));
        cmbFloor.setRenderer(new PlaceholderRenderer("-- Chọn tầng --"));
        cmbApartment.setRenderer(new PlaceholderRenderer("-- Chọn căn hộ --"));
        
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
    
    // ✅ NEW: Dynamic dates section
    private JPanel createDatesSection() {
        datesSection = createSection("📅 Thời Hạn Hợp Đồng");
        datesSection.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Signed Date (ALWAYS visible)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        lblSignedDate = createLabel("Ngày ký:", true);
        datesSection.add(lblSignedDate, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnSignedDate = createDateSpinner();
        datesSection.add(spnSignedDate, gbc);
        
        // Row 1 continued: Start Date (ONLY for RENTAL)
        gbc.gridx = 2; gbc.weightx = 0;
        lblStartDate = createLabel("Ngày bắt đầu:", true);
        datesSection.add(lblStartDate, gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        spnStartDate = createDateSpinner();
        datesSection.add(spnStartDate, gbc);
        
        // Row 2: End Date + Indefinite (ONLY for RENTAL)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        lblEndDate = createLabel("Ngày kết thúc:", true);
        datesSection.add(lblEndDate, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnEndDate = createDateSpinner();
        datesSection.add(spnEndDate, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0; gbc.gridwidth = 2;
        chkIndefinite = new JCheckBox("Vô thời hạn");
        chkIndefinite.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkIndefinite.setBackground(Color.WHITE);
        chkIndefinite.addActionListener(e -> spnEndDate.setEnabled(!chkIndefinite.isSelected()));
        datesSection.add(chkIndefinite, gbc);
        
        // Initial visibility: Show all for RENTAL (default)
        onContractTypeChanged();
        
        return datesSection;
    }
    
    // ✅ NEW: Dynamic financial section
    private JPanel createFinancialSection() {
        JPanel section = createSection("💰 Thông Tin Tài Chính");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        
        // Row 1: Deposit
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Tiền cọc:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtDepositAmount = MoneyFormatter.createMoneyField(32);
        section.add(txtDepositAmount, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblDepositCurrency = new JLabel("VNĐ");
        lblDepositCurrency.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDepositCurrency.setForeground(UIConstants.PRIMARY_COLOR);
        section.add(lblDepositCurrency, gbc);
        
        // Row 2: Price (dynamic label)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        lblPriceField = createLabel("Tiền thuê/tháng:", true); // Default
        section.add(lblPriceField, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtPriceAmount = MoneyFormatter.createMoneyField(32);
        section.add(txtPriceAmount, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblPriceCurrency = new JLabel("VNĐ");
        lblPriceCurrency.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPriceCurrency.setForeground(UIConstants.PRIMARY_COLOR);
        section.add(lblPriceCurrency, gbc);
        
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
    
    // CASCADE LOGIC
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
            List<Floor> floors = floorDAO.getFloorsByBuildingId(selected.building.getId());
            cmbFloor.removeAllItems();
            cmbFloor.addItem(null);
            for (Floor floor : floors) {
                cmbFloor.addItem(new FloorDisplay(floor));
            }
            cmbFloor.setSelectedIndex(0);
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);
            cmbApartment.setSelectedIndex(0);
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
            List<Apartment> apartments = apartmentDAO.getApartmentsByFloorId(selected.floor.getId());
            cmbApartment.removeAllItems();
            cmbApartment.addItem(null);
            for (Apartment apt : apartments) {
                if (isEditMode || "AVAILABLE".equals(apt.getStatus())) {
                    cmbApartment.addItem(new ApartmentDisplay(apt));
                }
            }
            cmbApartment.setSelectedIndex(0);
        } finally {
            isUpdatingCombos = false;
        }
    }
    
    private void loadData() {
        if (!isEditMode) {
            cmbBuilding.addItem(null);
        }
        
        List<Building> buildings = buildingDAO.getAllBuildings();
        for (Building building : buildings) {
            cmbBuilding.addItem(new BuildingDisplay(building));
        }
        
        if (!isEditMode) {
            cmbBuilding.setSelectedIndex(0);
        }
        
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
        txtContractNumber.setText(contract.getContractNumber());
        
        String type = contract.getContractType();
        cmbContractType.setSelectedItem("RENTAL".equals(type) ? "Thuê" : "Sở hữu");
        
        if (contract.getSignedDate() != null) {
            spnSignedDate.setValue(contract.getSignedDate());
        }
        
        // Load dates based on type
        if (contract.isRental()) {
            if (contract.getStartDate() != null) {
                spnStartDate.setValue(contract.getStartDate());
            }
            if (contract.getEndDate() != null) {
                spnEndDate.setValue(contract.getEndDate());
            } else {
                chkIndefinite.setSelected(true);
                spnEndDate.setEnabled(false);
            }
        }
        
        MoneyFormatter.setValue(txtDepositAmount, contract.getDepositAmount().longValue());
        
        if (contract.getMonthlyRent() != null) {
            MoneyFormatter.setValue(txtPriceAmount, contract.getMonthlyRent().longValue());
        }
        
        if (contract.getNotes() != null) {
            txtNotes.setText(contract.getNotes());
        }
        
        Apartment apartment = apartmentDAO.getApartmentById(contract.getApartmentId());
        if (apartment != null) {
            Floor floor = floorDAO.getFloorById(apartment.getFloorId());
            if (floor != null) {
                selectBuildingAndFloor(floor.getBuildingId(), floor.getId(), apartment.getId());
            }
        }
        
        loadContractServices();
    }
    
    private void selectBuildingAndFloor(Long buildingId, Long floorId, Long apartmentId) {
        isUpdatingCombos = true;
        try {
            for (int i = 0; i < cmbBuilding.getItemCount(); i++) {
                BuildingDisplay bd = cmbBuilding.getItemAt(i);
                if (bd != null && bd.building.getId().equals(buildingId)) {
                    cmbBuilding.setSelectedIndex(i);
                    break;
                }
            }
            
            for (int i = 0; i < cmbFloor.getItemCount(); i++) {
                FloorDisplay fd = cmbFloor.getItemAt(i);
                if (fd != null && fd.floor.getId().equals(floorId)) {
                    cmbFloor.setSelectedIndex(i);
                    break;
                }
            }
            
            for (int i = 0; i < cmbApartment.getItemCount(); i++) {
                ApartmentDisplay ad = cmbApartment.getItemAt(i);
                if (ad != null && ad.apartment.getId().equals(apartmentId)) {
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
    
    // ✅ VALIDATION (updated for contract type)
    private boolean validateForm() {
        if (cmbApartment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Resident validation
        if (txtResidentName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String phone = txtResidentPhone.getText().trim();
        if (phone.isEmpty() || !phone.matches("^[0-9]{10,11}$")) {
            JOptionPane.showMessageDialog(this, "SĐT không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String identityCard = txtResidentIdentityCard.getText().trim();
        if (identityCard.isEmpty() || (!identityCard.matches("^[0-9]{9}$") && !identityCard.matches("^[0-9]{12}$"))) {
            JOptionPane.showMessageDialog(this, "CCCD/CMND không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!isEditMode && residentDAO.isIdentityCardExists(identityCard)) {
            JOptionPane.showMessageDialog(this, "CCCD/CMND đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // ✅ NEW: Validate dates based on contract type
        String selectedType = (String) cmbContractType.getSelectedItem();
        boolean isRental = "Thuê".equals(selectedType);
        
        if (isRental) {
            // RENTAL: Need start and end dates
            Date startDate = (Date) spnStartDate.getValue();
            if (!chkIndefinite.isSelected()) {
                Date endDate = (Date) spnEndDate.getValue();
                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        // OWNERSHIP: No validation for start/end dates (they're hidden)
        
        // Deposit
        Long depositValue = MoneyFormatter.getValue(txtDepositAmount);
        if (depositValue == null || depositValue <= 0) {
            JOptionPane.showMessageDialog(this, "Tiền cọc không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Price
        Long priceValue = MoneyFormatter.getValue(txtPriceAmount);
        if (priceValue == null || priceValue <= 0) {
            String label = isRental ? "tiền thuê" : "giá mua";
            JOptionPane.showMessageDialog(this, "Vui lòng nhập " + label + "!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    // ✅ SAVE (updated for contract type)
    private void saveContract() {
        if (!validateForm()) return;
        
        try {
            // Create resident
            Resident newResident = new Resident();
            newResident.setFullName(txtResidentName.getText().trim());
            newResident.setPhone(txtResidentPhone.getText().trim());
            newResident.setIdentityCard(txtResidentIdentityCard.getText().trim());
            newResident.setGender((String) cmbResidentGender.getSelectedItem());
            newResident.setDob((Date) spnResidentDob.getValue());
            newResident.setEmail(txtResidentEmail.getText().trim().isEmpty() ? null : txtResidentEmail.getText().trim());
            
            if (!residentDAO.insertResident(newResident) || newResident.getId() == null) {
                JOptionPane.showMessageDialog(this, "Không thể tạo chủ hộ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create contract
            ApartmentDisplay ad = (ApartmentDisplay) cmbApartment.getSelectedItem();
            contract.setApartmentId(ad.apartment.getId());
            contract.setResidentId(newResident.getId());
            
            String typeDisplay = (String) cmbContractType.getSelectedItem();
            contract.setContractType("Thuê".equals(typeDisplay) ? "RENTAL" : "OWNERSHIP");
            
            contract.setSignedDate((Date) spnSignedDate.getValue());
            
            // ✅ Set dates based on contract type
            if (contract.isRental()) {
                contract.setStartDate((Date) spnStartDate.getValue());
                contract.setEndDate(chkIndefinite.isSelected() ? null : (Date) spnEndDate.getValue());
            } else {
                // OWNERSHIP: No start/end dates
                contract.setStartDate(null);
                contract.setEndDate(null);
            }
            
            contract.setDepositAmount(BigDecimal.valueOf(MoneyFormatter.getValue(txtDepositAmount)));
            contract.setMonthlyRent(BigDecimal.valueOf(MoneyFormatter.getValue(txtPriceAmount)));
            contract.setNotes(txtNotes.getText().trim());
            contract.setStatus("ACTIVE");
            
            if (!isEditMode) {
                contract.setContractNumber(contractDAO.generateContractNumber());
            }
            
            boolean success = isEditMode ? contractDAO.updateContract(contract) : contractDAO.insertContract(contract);
            
            if (success) {
                if (contract.getId() != null) {
                    saveContractServices();
                }
                isConfirmed = true;
                JOptionPane.showMessageDialog(this, "Lưu hợp đồng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu hợp đồng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveContractServices() {
        try {
            List<Long> selectedServiceIds = new ArrayList<>();
            for (JCheckBox cb : serviceCheckboxes) {
                if (cb.isSelected()) {
                    Service service = (Service) cb.getClientProperty("service");
                    if (service != null && service.getId() != null) {
                        selectedServiceIds.add(service.getId());
                    }
                }
            }
            
            if (!selectedServiceIds.isEmpty()) {
                Date appliedDate = contract.getStartDate() != null ? contract.getStartDate() : new Date();
                contractServiceDAO.insertServicesForContract(contract.getId(), selectedServiceIds, appliedDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // HELPERS
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(66, 66, 66)
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
    
    // INNER CLASSES
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
    
    private class PlaceholderRenderer extends DefaultListCellRenderer {
        private String placeholder;
        
        PlaceholderRenderer(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText(placeholder);
                setForeground(new Color(158, 158, 158));
            }
            return this;
        }
    }
    
    public boolean isConfirmed() {
        return isConfirmed;
    }
    
    public Contract getContract() {
        return contract;
    }
}