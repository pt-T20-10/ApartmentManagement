package view;

import dao.*;
import model.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialog for creating or editing contracts
 */
public class ContractFormDialog extends JDialog {
    
    private ContractDAO contractDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private ServiceDAO serviceDAO;
    private ContractServiceDAO contractServiceDAO;
    
    private Contract contract;
    private boolean isEditMode;
    private boolean isConfirmed = false;
    
    // Form components
    private JTextField txtContractNumber;
    private JComboBox<String> cmbContractType;
    private JComboBox<ApartmentDisplay> cmbApartment;
    private JComboBox<ResidentDisplay> cmbResident;
    private JSpinner spnSignedDate;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JCheckBox chkIndefinite;
    private JTextField txtDepositAmount;
    private JTextArea txtNotes;
    
    // Service selection
    private JPanel servicesPanel;
    private List<JCheckBox> serviceCheckboxes;
    private List<Service> allServices;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    public ContractFormDialog(JFrame parent, Contract contract) {
        super(parent, "Hợp Đồng", true);
        
        this.contractDAO = new ContractDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.residentDAO = new ResidentDAO();
        this.serviceDAO = new ServiceDAO();
        this.contractServiceDAO = new ContractServiceDAO();
        this.serviceCheckboxes = new ArrayList<>();
        
        this.contract = contract != null ? contract : new Contract();
        this.isEditMode = contract != null && contract.getId() != null;
        
        initComponents();
        loadData();
        
        if (isEditMode) {
            loadContractData();
        }
        
        setSize(800, 850);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Header
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Contract Info Section
        mainPanel.add(createContractInfoSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Dates Section
        mainPanel.add(createDatesSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Financial Section
        mainPanel.add(createFinancialSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Services Section
        mainPanel.add(createServicesSection());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Notes Section
        mainPanel.add(createNotesSection());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel iconLabel = new JLabel(isEditMode ? "✏️" : "➕");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel(isEditMode ? "Chỉnh Sửa Hợp Đồng" : "Tạo Hợp Đồng Mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    private JPanel createContractInfoSection() {
        JPanel section = createSection("📋 Thông Tin Hợp Đồng");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Row 1: Contract Number + Type
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Số hợp đồng:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtContractNumber = new JTextField();
        txtContractNumber.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContractNumber.setPreferredSize(new Dimension(0, 35));
        txtContractNumber.setEnabled(false);
        txtContractNumber.setBackground(new Color(245, 245, 245));
        txtContractNumber.setText(isEditMode ? "" : "Tự động tạo");
        section.add(txtContractNumber, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Loại hợp đồng:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        cmbContractType = new JComboBox<>(new String[]{"Thuê", "Sở hữu"});
        cmbContractType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbContractType.setPreferredSize(new Dimension(0, 35));
        section.add(cmbContractType, gbc);
        
        // Row 2: Apartment + Resident
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        section.add(createLabel("Căn hộ:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        cmbApartment = new JComboBox<>();
        cmbApartment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbApartment.setPreferredSize(new Dimension(0, 35));
        if (!isEditMode) {
            // Add placeholder hint
            cmbApartment.setToolTipText("Chỉ hiển thị căn hộ đang trống");
        }
        section.add(cmbApartment, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Chủ hộ:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        cmbResident = new JComboBox<>();
        cmbResident.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbResident.setPreferredSize(new Dimension(0, 35));
        if (!isEditMode) {
            // Add placeholder hint
            cmbResident.setToolTipText("Chỉ hiển thị cư dân chưa có hợp đồng");
        }
        section.add(cmbResident, gbc);
        
        return section;
    }
    
    private JPanel createDatesSection() {
        JPanel section = createSection("📅 Thời Hạn Hợp Đồng");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Row 1: Signed Date
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Ngày ký:", false), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnSignedDate = createDateSpinner();
        section.add(spnSignedDate, gbc);
        
        // Row 2: Start Date + End Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        section.add(createLabel("Ngày bắt đầu:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        spnStartDate = createDateSpinner();
        section.add(spnStartDate, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        section.add(createLabel("Ngày kết thúc:", true), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        spnEndDate = createDateSpinner();
        section.add(spnEndDate, gbc);
        
        // Row 3: Indefinite checkbox
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        chkIndefinite = new JCheckBox("Hợp đồng vô thời hạn");
        chkIndefinite.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkIndefinite.setBackground(Color.WHITE);
        chkIndefinite.addActionListener(e -> {
            spnEndDate.setEnabled(!chkIndefinite.isSelected());
        });
        section.add(chkIndefinite, gbc);
        
        return section;
    }
    
    private JPanel createFinancialSection() {
        JPanel section = createSection("💰 Thông Tin Tài Chính");
        section.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        section.add(createLabel("Tiền cọc:", true), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtDepositAmount = new JTextField();
        txtDepositAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDepositAmount.setPreferredSize(new Dimension(0, 35));
        section.add(txtDepositAmount, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblVND = new JLabel("VNĐ");
        lblVND.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVND.setForeground(UIConstants.PRIMARY_COLOR);
        section.add(lblVND, gbc);
        
        return section;
    }
    
    private JPanel createServicesSection() {
        JPanel section = createSection("🔧 Dịch Vụ Áp Dụng");
        section.setLayout(new BorderLayout(0, 10));
        
        JLabel lblInfo = new JLabel("Chọn các dịch vụ áp dụng cho hợp đồng này:");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblInfo.setForeground(new Color(117, 117, 117));
        lblInfo.setBorder(new EmptyBorder(5, 10, 5, 10));
        section.add(lblInfo, BorderLayout.NORTH);
        
        servicesPanel = new JPanel();
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.Y_AXIS));
        servicesPanel.setBackground(Color.WHITE);
        servicesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(servicesPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(0, 150));
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createNotesSection() {
        JPanel section = createSection("📝 Ghi Chú");
        section.setLayout(new BorderLayout(0, 10));
        
        txtNotes = new JTextArea(4, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(txtNotes);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        
        JButton btnCancel = createButton("Hủy", new Color(158, 158, 158));
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSave = createButton(isEditMode ? "💾 Lưu" : "✅ Tạo", UIConstants.PRIMARY_COLOR);
        btnSave.addActionListener(e -> saveContract());
        
        panel.add(btnCancel);
        panel.add(btnSave);
        
        return panel;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Check if a resident already has an active contract
     */
    private boolean hasActiveContract(Long residentId) {
        List<Contract> allContracts = contractDAO.getAllContracts();
        for (Contract c : allContracts) {
            if (c.getResidentId().equals(residentId) && c.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 15),
            new Color(66, 66, 66)
        ));
        return panel;
    }
    
    private JLabel createLabel(String text, boolean required) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        spinner.setPreferredSize(new Dimension(0, 35));
        return spinner;
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // ===== DATA LOADING =====
    
    private void loadData() {
        // Load apartments (only AVAILABLE if creating new)
        List<Apartment> apartments;
        if (isEditMode) {
            // In edit mode: show current apartment + all available apartments
            apartments = apartmentDAO.getAllApartments().stream()
                .filter(a -> a.getId().equals(contract.getApartmentId()) || "AVAILABLE".equals(a.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        } else {
            // In create mode: only show available apartments
            apartments = apartmentDAO.getAllApartments().stream()
                .filter(a -> "AVAILABLE".equals(a.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        cmbApartment.removeAllItems();
        
        if (!isEditMode && apartments.isEmpty()) {
            // Show warning if no apartments available
            JOptionPane.showMessageDialog(this,
                "Không có căn hộ trống nào!\nVui lòng đợi có căn hộ trống hoặc kết thúc hợp đồng cũ.",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
        }
        
        for (Apartment apt : apartments) {
            cmbApartment.addItem(new ApartmentDisplay(apt));
        }
        
        // Load residents (only those without active contracts if creating new)
        List<Resident> allResidents = residentDAO.getAllResidents();
        List<Resident> availableResidents = new ArrayList<>();
        
        if (isEditMode) {
            // In edit mode: show current resident + residents without active contracts
            for (Resident resident : allResidents) {
                if (resident.getId().equals(contract.getResidentId())) {
                    // Always include current resident
                    availableResidents.add(resident);
                } else {
                    // Check if resident has active contract
                    if (!hasActiveContract(resident.getId())) {
                        availableResidents.add(resident);
                    }
                }
            }
        } else {
            // In create mode: only show residents without active contracts
            for (Resident resident : allResidents) {
                if (!hasActiveContract(resident.getId())) {
                    availableResidents.add(resident);
                }
            }
        }
        
        cmbResident.removeAllItems();
        
        if (!isEditMode && availableResidents.isEmpty()) {
            // Show warning if no residents available
            JOptionPane.showMessageDialog(this,
                "Không có cư dân nào chưa có hợp đồng!\nTất cả cư dân đã có hợp đồng đang hiệu lực.",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
        }
        
        for (Resident resident : availableResidents) {
            cmbResident.addItem(new ResidentDisplay(resident));
        }
        
        // Load services
        allServices = serviceDAO.getAllServices();
        servicesPanel.removeAll();
        serviceCheckboxes.clear();
        
        for (Service service : allServices) {
            JCheckBox chk = new JCheckBox();
            chk.setText(String.format("%s (%s VNĐ/%s)", 
                service.getServiceName(),
                service.getUnitPrice(),
                service.getUnit()));
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            chk.setBackground(Color.WHITE);
            chk.setSelected(service.isMandatory()); // Auto select mandatory
            chk.setEnabled(!service.isMandatory()); // Disable mandatory
            
            serviceCheckboxes.add(chk);
            servicesPanel.add(chk);
        }
        
        servicesPanel.revalidate();
        servicesPanel.repaint();
    }
    
    private void loadContractData() {
        // Contract number
        txtContractNumber.setText(contract.getContractNumber());
        
        // Contract type
        cmbContractType.setSelectedItem(contract.getContractTypeDisplay());
        
        // Apartment
        for (int i = 0; i < cmbApartment.getItemCount(); i++) {
            ApartmentDisplay ad = cmbApartment.getItemAt(i);
            if (ad.apartment.getId().equals(contract.getApartmentId())) {
                cmbApartment.setSelectedIndex(i);
                break;
            }
        }
        
        // Resident
        for (int i = 0; i < cmbResident.getItemCount(); i++) {
            ResidentDisplay rd = cmbResident.getItemAt(i);
            if (rd.resident.getId().equals(contract.getResidentId())) {
                cmbResident.setSelectedIndex(i);
                break;
            }
        }
        
        // Dates
        if (contract.getSignedDate() != null) {
            spnSignedDate.setValue(contract.getSignedDate());
        }
        spnStartDate.setValue(contract.getStartDate());
        
        if (contract.getEndDate() != null) {
            spnEndDate.setValue(contract.getEndDate());
            chkIndefinite.setSelected(false);
        } else {
            chkIndefinite.setSelected(true);
            spnEndDate.setEnabled(false);
        }
        
        // Deposit
        if (contract.getDepositAmount() != null) {
            txtDepositAmount.setText(contract.getDepositAmount().toString());
        }
        
        // Notes
        if (contract.getNotes() != null) {
            txtNotes.setText(contract.getNotes());
        }
        
        // Services
        if (contract.getId() != null) {
            List<ContractService> contractServices = contractServiceDAO.getActiveServicesByContract(contract.getId());
            for (ContractService cs : contractServices) {
                for (int i = 0; i < allServices.size(); i++) {
                    if (allServices.get(i).getId().equals(cs.getServiceId())) {
                        serviceCheckboxes.get(i).setSelected(true);
                        break;
                    }
                }
            }
        }
    }
    
    // ===== VALIDATION & SAVE =====
    
    private boolean validateForm() {
        // Check if there are any apartments available
        if (cmbApartment.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không có căn hộ trống nào!\nVui lòng đợi có căn hộ trống hoặc kết thúc hợp đồng cũ.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if there are any residents available
        if (cmbResident.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Không có cư dân nào chưa có hợp đồng!\nTất cả cư dân đã có hợp đồng đang hiệu lực.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Apartment
        if (cmbApartment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Resident
        if (cmbResident.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chủ hộ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        
        // Deposit amount
        String depositStr = txtDepositAmount.getText().trim();
        if (depositStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiền cọc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            new BigDecimal(depositStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Tiền cọc không hợp lệ!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if apartment already has active contract (only for new contracts)
        if (!isEditMode) {
            ApartmentDisplay ad = (ApartmentDisplay) cmbApartment.getSelectedItem();
            if (contractDAO.hasActiveContract(ad.apartment.getId())) {
                JOptionPane.showMessageDialog(this,
                    "Căn hộ này đã có hợp đồng đang hiệu lực!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    private void saveContract() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Get form data
            ApartmentDisplay ad = (ApartmentDisplay) cmbApartment.getSelectedItem();
            ResidentDisplay rd = (ResidentDisplay) cmbResident.getSelectedItem();
            
            contract.setApartmentId(ad.apartment.getId());
            contract.setResidentId(rd.resident.getId());
            
            String typeDisplay = (String) cmbContractType.getSelectedItem();
            contract.setContractType("Thuê".equals(typeDisplay) ? "RENTAL" : "OWNERSHIP");
            
            contract.setSignedDate((Date) spnSignedDate.getValue());
            contract.setStartDate((Date) spnStartDate.getValue());
            
            if (chkIndefinite.isSelected()) {
                contract.setEndDate(null);
            } else {
                contract.setEndDate((Date) spnEndDate.getValue());
            }
            
            contract.setDepositAmount(new BigDecimal(txtDepositAmount.getText().trim()));
            contract.setNotes(txtNotes.getText().trim());
            contract.setStatus("ACTIVE");
            
            // Generate contract number if new
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
                JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật hợp đồng thành công!" : "Tạo hợp đồng thành công!",
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
        // Get selected service IDs
        List<Long> selectedServiceIds = new ArrayList<>();
        for (int i = 0; i < serviceCheckboxes.size(); i++) {
            if (serviceCheckboxes.get(i).isSelected()) {
                selectedServiceIds.add(allServices.get(i).getId());
            }
        }
        
        // Insert services
        Date appliedDate = contract.getStartDate();
        contractServiceDAO.insertServicesForContract(contract.getId(), selectedServiceIds, appliedDate);
    }
    
    // ===== GETTERS =====
    
    public boolean isConfirmed() {
        return isConfirmed;
    }
    
    public Contract getContract() {
        return contract;
    }
    
    // ===== INNER CLASSES =====
    
    private class ApartmentDisplay {
        Apartment apartment;
        
        ApartmentDisplay(Apartment apartment) {
            this.apartment = apartment;
        }
        
        @Override
        public String toString() {
            return apartment.getRoomNumber() + " (" + apartment.getArea() + "m²)";
        }
    }
    
    private class ResidentDisplay {
        Resident resident;
        
        ResidentDisplay(Resident resident) {
            this.resident = resident;
        }
        
        @Override
        public String toString() {
            return resident.getFullName() + " - " + resident.getPhone();
        }
    }
}