package view;

import dao.ServiceDAO;
import model.Service;
import util.UIConstants;
import util.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ServiceManagementPanel extends JPanel {
    
    private ServiceDAO serviceDAO;
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    private JTextField nameField;
    private JTextField unitField;
    private JTextField unitPriceField;
    private JCheckBox isMandatoryCheckbox;
    private JTextArea descriptionArea;
    
    private Service selectedService = null;
    
    public ServiceManagementPanel() {
        this.serviceDAO = new ServiceDAO();
        
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        createHeader();
        createTablePanel();
        createFormPanel();
        loadServices();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JLabel iconLabel = new JLabel("[S]");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel titleLabel = new JLabel("Quan Ly Dich Vu");
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
        
        ModernButton searchButton = new ModernButton("[T] Tim Kiem", UIConstants.INFO_COLOR);
        searchButton.addActionListener(e -> searchServices());
        
        ModernButton refreshButton = new ModernButton("[R] Lam Moi", UIConstants.SUCCESS_COLOR);
        refreshButton.addActionListener(e -> { searchField.setText(""); loadServices(); });
        
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
        
        String[] columns = {"ID", "Ten Dich Vu", "Don Vi", "Don Gia", "Bat Buoc", "Mo Ta"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        serviceTable = new JTable(tableModel);
        serviceTable.setFont(UIConstants.FONT_REGULAR);
        serviceTable.setRowHeight(40);
        serviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceTable.setShowGrid(true);
        serviceTable.setGridColor(UIConstants.BORDER_COLOR);
        serviceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedService();
        });
        
        JTableHeader header = serviceTable.getTableHeader();
        header.setFont(UIConstants.FONT_HEADING);
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(serviceTable);
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
        
        JLabel formTitle = new JLabel("Thong Tin Dich Vu");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        formPanel.add(createFieldLabel("Ten Dich Vu *"));
        nameField = createTextField();
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createFieldLabel("Don Vi (kWh, m3, thang) *"));
        unitField = createTextField();
        formPanel.add(unitField);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createFieldLabel("Don Gia *"));
        unitPriceField = createTextField();
        formPanel.add(unitPriceField);
        formPanel.add(Box.createVerticalStrut(15));
        
        isMandatoryCheckbox = new JCheckBox("Bat buoc?");
        isMandatoryCheckbox.setFont(UIConstants.FONT_REGULAR);
        isMandatoryCheckbox.setBackground(Color.WHITE);
        isMandatoryCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(isMandatoryCheckbox);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(createFieldLabel("Mo Ta"));
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(UIConstants.FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        formPanel.add(descScrollPane);
        formPanel.add(Box.createVerticalStrut(20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton addButton = new ModernButton("[+] Them", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> addService());
        
        ModernButton updateButton = new ModernButton("[E] Sua", UIConstants.WARNING_COLOR);
        updateButton.addActionListener(e -> updateService());
        
        ModernButton deleteButton = new ModernButton("[X] Xoa", UIConstants.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteService());
        
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
    
    private void loadServices() {
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        
        for (Service service : services) {
            Object[] row = {
                service.getId(),
                service.getName(),
                service.getUnit(),
                service.getUnitPrice(),
                service.isMandatory() ? "Yes" : "No",
                service.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void loadSelectedService() {
        int selectedRow = serviceTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            selectedService = serviceDAO.getServiceById(id);
            
            if (selectedService != null) {
                nameField.setText(selectedService.getName());
                unitField.setText(selectedService.getUnit());
                unitPriceField.setText(selectedService.getUnitPrice().toString());
                isMandatoryCheckbox.setSelected(selectedService.isMandatory());
                descriptionArea.setText(selectedService.getDescription());
            }
        }
    }
    
    private void addService() {
        if (!validateForm()) return;
        
        Service service = new Service();
        service.setName(nameField.getText().trim());
        service.setUnit(unitField.getText().trim());
        service.setUnitPrice(new BigDecimal(unitPriceField.getText().trim()));
        service.setMandatory(isMandatoryCheckbox.isSelected());
        service.setDescription(descriptionArea.getText().trim());
        
        if (serviceDAO.insertService(service)) {
            JOptionPane.showMessageDialog(this, "Them dich vu thanh cong!");
            clearForm();
            loadServices();
        } else {
            JOptionPane.showMessageDialog(this, "Them that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateService() {
        if (selectedService == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon dich vu can sua!");
            return;
        }
        
        if (!validateForm()) return;
        
        selectedService.setName(nameField.getText().trim());
        selectedService.setUnit(unitField.getText().trim());
        selectedService.setUnitPrice(new BigDecimal(unitPriceField.getText().trim()));
        selectedService.setMandatory(isMandatoryCheckbox.isSelected());
        selectedService.setDescription(descriptionArea.getText().trim());
        
        if (serviceDAO.updateService(selectedService)) {
            JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!");
            clearForm();
            loadServices();
        } else {
            JOptionPane.showMessageDialog(this, "Cap nhat that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteService() {
        if (selectedService == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon dich vu can xoa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac chan muon xoa dich vu '" + selectedService.getName() + "'?",
            "Xac Nhan Xoa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (serviceDAO.deleteService(selectedService.getId())) {
                JOptionPane.showMessageDialog(this, "Xoa thanh cong!");
                clearForm();
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this, "Xoa that bai!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchServices() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadServices();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        
        for (Service service : services) {
            if (service.getName().toLowerCase().contains(keyword) ||
                service.getUnit().toLowerCase().contains(keyword)) {
                Object[] row = {
                    service.getId(),
                    service.getName(),
                    service.getUnit(),
                    service.getUnitPrice(),
                    service.isMandatory() ? "Yes" : "No",
                    service.getDescription()
                };
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Khong tim thay dich vu nao!");
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        unitField.setText("");
        unitPriceField.setText("");
        isMandatoryCheckbox.setSelected(false);
        descriptionArea.setText("");
        selectedService = null;
        serviceTable.clearSelection();
    }
    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ten dich vu!");
            nameField.requestFocus();
            return false;
        }
        
        if (unitField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap don vi!");
            unitField.requestFocus();
            return false;
        }
        
        if (unitPriceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap don gia!");
            unitPriceField.requestFocus();
            return false;
        }
        
        try {
            BigDecimal price = new BigDecimal(unitPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Don gia phai lon hon 0!");
                unitPriceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Don gia khong hop le!");
            unitPriceField.requestFocus();
            return false;
        }
        
        return true;
    }
}