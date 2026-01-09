package view;

import dao.*;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Dashboard Panel - Completely Fixed and Working
 */
public class DashboardPanel extends JPanel {
    private final Color CARD_BG = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(76, 132, 255);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WARNING_COLOR = new Color(255, 193, 7);
    private final Color DANGER_COLOR = new Color(220, 53, 69);
    private final Color INFO_COLOR = new Color(23, 162, 184);
    private final Color PURPLE_COLOR = new Color(111, 66, 193);
    
    private BuildingDAO buildingDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private ContractDAO contractDAO;
    private InvoiceDAO invoiceDAO;
    
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        // Initialize DAOs
        buildingDAO = new BuildingDAO();
        apartmentDAO = new ApartmentDAO();
        residentDAO = new ResidentDAO();
        contractDAO = new ContractDAO();
        invoiceDAO = new InvoiceDAO();
        
        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        
        // Main container with padding
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(new Color(245, 247, 250));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        
        // Row 1: Building, Total Apartments, Available
        gbc.gridx = 0; gbc.gridy = 0;
        mainContainer.add(createStatCard("Tòa Nhà", String.valueOf(buildingDAO.countBuildings()), 
            "\u25A3", PRIMARY_COLOR), gbc);
        
        gbc.gridx = 1;
        mainContainer.add(createStatCard("Tổng Căn Hộ", String.valueOf(apartmentDAO.countApartments()), 
            "\u2302", INFO_COLOR), gbc);
        
        gbc.gridx = 2;
        mainContainer.add(createStatCard("Đang Trống", String.valueOf(apartmentDAO.countAvailableApartments()), 
            "\u2713", SUCCESS_COLOR), gbc);
        
        // Row 2: Rented, Residents, Active Contracts
        gbc.gridx = 0; gbc.gridy = 1;
        mainContainer.add(createStatCard("Đã Cho Thuê", String.valueOf(apartmentDAO.countRentedApartments()), 
            "\u2612", WARNING_COLOR), gbc);
        
        gbc.gridx = 1;
        mainContainer.add(createStatCard("Cư Dân", String.valueOf(residentDAO.countResidents()), 
            "\u265B", PURPLE_COLOR), gbc);
        
        gbc.gridx = 2;
        mainContainer.add(createStatCard("Hợp Đồng", String.valueOf(contractDAO.countActiveContracts()), 
            "\u2709", PRIMARY_COLOR), gbc);
        
        // Row 3: Unpaid Invoices, Monthly Revenue
        gbc.gridx = 0; gbc.gridy = 2;
        mainContainer.add(createStatCard("Hóa Đơn Chưa Thu", String.valueOf(invoiceDAO.countUnpaidInvoices()), 
            "\u26A0", DANGER_COLOR), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        mainContainer.add(createRevenueCard("Doanh Thu Tháng", 
            formatCurrency(invoiceDAO.getMonthlyRevenue(currentMonth, currentYear)), 
            "\u263C", SUCCESS_COLOR), gbc);
        
        // Add vertical space to push content to top
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        mainContainer.add(spacer, gbc);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private JPanel createStatCard(String title, String value, String iconChar, Color color) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(280, 110));
        
        // Icon panel
        JPanel iconPanel = createIconPanel(iconChar, color, 65);
        
        // Text panel
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(CARD_BG);
        
        JPanel textContent = new JPanel();
        textContent.setLayout(new BoxLayout(textContent, BoxLayout.Y_AXIS));
        textContent.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(new Color(33, 37, 41));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textContent.add(titleLabel);
        textContent.add(Box.createVerticalStrut(3));
        textContent.add(valueLabel);
        
        textPanel.add(textContent);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 2),
                    BorderFactory.createEmptyBorder(19, 19, 19, 19)
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return card;
    }
    
    private JPanel createRevenueCard(String title, String value, String iconChar, Color color) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(280, 110));
        
        // Icon panel
        JPanel iconPanel = createIconPanel(iconChar, color, 65);
        
        // Text panel
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(CARD_BG);
        
        JPanel textContent = new JPanel();
        textContent.setLayout(new BoxLayout(textContent, BoxLayout.Y_AXIS));
        textContent.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textContent.add(titleLabel);
        textContent.add(Box.createVerticalStrut(3));
        textContent.add(valueLabel);
        
        textPanel.add(textContent);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 2),
                    BorderFactory.createEmptyBorder(19, 19, 19, 19)
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return card;
    }
    
    private JPanel createIconPanel(String iconChar, Color color, int circleSize) {
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2d.fillOval(centerX - circleSize/2, centerY - circleSize/2, circleSize, circleSize);
            }
        };
        
        iconPanel.setPreferredSize(new Dimension(70, 70));
        iconPanel.setMinimumSize(new Dimension(70, 70));
        iconPanel.setMaximumSize(new Dimension(70, 70));
        iconPanel.setBackground(CARD_BG);
        iconPanel.setLayout(new GridBagLayout());
        
        JLabel iconLabel = new JLabel(iconChar);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 32));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        iconPanel.add(iconLabel);
        
        return iconPanel;
    }
    
    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) {
            amount = java.math.BigDecimal.ZERO;
        }
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(amount);
        
        return formatted + " đ";
    }
}