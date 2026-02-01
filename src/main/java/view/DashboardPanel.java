package view;

import dao.*;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final Color CARD_BG = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(76, 132, 255);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WARNING_COLOR = new Color(255, 193, 7);
    private final Color DANGER_COLOR = new Color(220, 53, 69);
    private final Color INFO_COLOR = new Color(23, 162, 184);
    private final Color PURPLE_COLOR = new Color(111, 66, 193);

    private final DashboardNavigator navigator;

    private BuildingDAO buildingDAO;
    private ApartmentDAO apartmentDAO;
    private ResidentDAO residentDAO;
    private ContractDAO contractDAO;
    private InvoiceDAO invoiceDAO;

    public DashboardPanel(DashboardNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        buildingDAO = new BuildingDAO();
        apartmentDAO = new ApartmentDAO();
        residentDAO = new ResidentDAO();
        contractDAO = new ContractDAO();
        invoiceDAO = new InvoiceDAO();

        // Create scrollable content
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 247, 250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(245, 247, 250));

        // Header
        main.add(createDashboardHeader());
        main.add(Box.createVerticalStrut(20));

        // KPI Cards
        main.add(createKPISection());
        main.add(Box.createVerticalStrut(20));

        // Charts
        main.add(createChartsSection());
        
        wrapper.add(main, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createDashboardHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel title = new JLabel("Dashboard Tổng Quan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(33, 37, 41));

        header.add(title, BorderLayout.WEST);

        return header;
    }

    private JPanel createKPISection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 247, 250));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Row 1: 3 cards
        JPanel row1 = new JPanel(new GridLayout(1, 3, 15, 0));
        row1.setBackground(new Color(245, 247, 250));
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        row1.add(createStatCard("Tòa Nhà",
                String.valueOf(buildingDAO.countBuildings()),
                "🏢", PRIMARY_COLOR, navigator::goToBuildings));
        row1.add(createStatCard("Tổng Căn Hộ",
                String.valueOf(apartmentDAO.countApartments()),
                "🏠", INFO_COLOR, navigator::goToApartments));
        row1.add(createStatCard("Đang Trống",
                String.valueOf(apartmentDAO.countAvailableApartments()),
                "✓", SUCCESS_COLOR, navigator::goToApartments));

        // Row 2: 3 cards
        JPanel row2 = new JPanel(new GridLayout(1, 3, 15, 0));
        row2.setBackground(new Color(245, 247, 250));
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        row2.add(createStatCard("Đã Cho Thuê",
                String.valueOf(apartmentDAO.countRentedApartments()),
                "☑", WARNING_COLOR, navigator::goToContracts));
        row2.add(createStatCard("Cư Dân",
                String.valueOf(residentDAO.countResidents()),
                "👥", PURPLE_COLOR, navigator::goToResidents));
        row2.add(createStatCard("Hợp Đồng",
                String.valueOf(contractDAO.countActiveContracts()),
                "📋", PRIMARY_COLOR, navigator::goToContracts));

        // Row 3: 2 larger cards
        JPanel row3 = new JPanel(new GridLayout(1, 2, 15, 0));
        row3.setBackground(new Color(245, 247, 250));
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        row3.add(createRevenueCard());
        row3.add(createUnpaidCard());

        section.add(row1);
        section.add(Box.createVerticalStrut(15));
        section.add(row2);
        section.add(Box.createVerticalStrut(15));
        section.add(row3);

        return section;
    }

    private JPanel createChartsSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 15, 0));
        section.setBackground(new Color(245, 247, 250));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        section.add(createRevenueChart());
        section.add(createInvoiceStatusChart());

        return section;
    }

    private JPanel createStatCard(String title, String value, String icon, 
                                   Color color, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon
        card.add(createIconPanel(icon, color, 55), BorderLayout.WEST);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(CARD_BG);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(108, 117, 125));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(new Color(33, 37, 41));
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblValue);

        card.add(textPanel, BorderLayout.CENTER);

        // Hover effect
        if (onClick != null) {
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onClick.run();
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(new Color(248, 249, 250));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(19, 19, 19, 19)
                    ));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(CARD_BG);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                    ));
                }
            });
        }

        return card;
    }

    private JPanel createRevenueCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        card.add(createIconPanel("💰", SUCCESS_COLOR, 65), BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(CARD_BG);

        JLabel lblTitle = new JLabel("Tổng Doanh Thu");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(108, 117, 125));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        BigDecimal revenue = invoiceDAO.getTotalRevenue();
        JLabel lblValue = new JLabel(formatCurrency(revenue));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(SUCCESS_COLOR);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(lblValue);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUnpaidCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(createIconPanel("⚠", DANGER_COLOR, 65), BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(CARD_BG);

        JLabel lblTitle = new JLabel("Hóa Đơn Chưa Thu");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(108, 117, 125));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(String.valueOf(invoiceDAO.countUnpaidInvoices()));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(DANGER_COLOR);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(lblValue);

        card.add(textPanel, BorderLayout.CENTER);

        // Click to invoices
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (navigator != null) {
                    navigator.goToInvoices();
                }
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_BG);
            }
        });

        return card;
    }

    private JPanel createRevenueChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel title = new JLabel("📊 Doanh Thu 12 Tháng Gần Nhất");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(33, 37, 41));

        // Chart content
        JPanel chartContent = createLineChart();

        chartPanel.add(title, BorderLayout.NORTH);
        chartPanel.add(Box.createVerticalStrut(15), BorderLayout.AFTER_LINE_ENDS);
        chartPanel.add(chartContent, BorderLayout.CENTER);

        return chartPanel;
    }

    private JPanel createLineChart() {
        // Get revenue data for last 12 months
        Calendar cal = Calendar.getInstance();

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -i);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            labels.add(month + "/" + (year % 100));
            BigDecimal revenue = invoiceDAO.getMonthlyRevenue(month, year);
            values.add(revenue);
        }

        return new LineChartPanel(labels, values, SUCCESS_COLOR);
    }

    private JPanel createInvoiceStatusChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel title = new JLabel("📈 Trạng Thái Hóa Đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(33, 37, 41));

        // Get data
        List<model.Invoice> allInvoices = invoiceDAO.getAllInvoices();
        int paid = (int) allInvoices.stream().filter(i -> "PAID".equals(i.getStatus())).count();
        int unpaid = (int) allInvoices.stream().filter(i -> "UNPAID".equals(i.getStatus())).count();

        // Chart content
        JPanel chartContent = createPieChart(paid, unpaid);

        chartPanel.add(title, BorderLayout.NORTH);
        chartPanel.add(Box.createVerticalStrut(15), BorderLayout.AFTER_LINE_ENDS);
        chartPanel.add(chartContent, BorderLayout.CENTER);

        return chartPanel;
    }

    private JPanel createPieChart(int paid, int unpaid) {
        return new PieChartPanel(paid, unpaid, SUCCESS_COLOR, DANGER_COLOR);
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
                g2d.fillOval(centerX - circleSize / 2, centerY - circleSize / 2, circleSize, circleSize);
            }
        };

        iconPanel.setPreferredSize(new Dimension(75, 75));
        iconPanel.setMinimumSize(new Dimension(75, 75));
        iconPanel.setMaximumSize(new Dimension(75, 75));
        iconPanel.setBackground(CARD_BG);
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(iconChar);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        iconPanel.add(iconLabel);

        return iconPanel;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " đ";
    }

    // ===== INNER CLASS: LINE CHART =====
    private class LineChartPanel extends JPanel {
        private List<String> labels;
        private List<BigDecimal> values;
        private Color lineColor;

        public LineChartPanel(List<String> labels, List<BigDecimal> values, Color lineColor) {
            this.labels = labels;
            this.values = values;
            this.lineColor = lineColor;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(500, 280));
            setMinimumSize(new Dimension(400, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 50;
            int labelPadding = 25;
            int width = getWidth();
            int height = getHeight();

            // Check if we have data
            if (values == null || values.isEmpty()) {
                g2d.setColor(new Color(100, 100, 100));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "Chưa có dữ liệu";
                int msgWidth = g2d.getFontMetrics().stringWidth(msg);
                g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
                return;
            }

            // Find max value
            BigDecimal maxValue = values.stream()
                .filter(v -> v != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
            
            if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
                maxValue = BigDecimal.valueOf(1000000);
            }

            // Draw axes
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(padding, height - padding, width - padding, height - padding); // X axis
            g2d.drawLine(padding, padding, padding, height - padding); // Y axis

            // Draw grid lines
            g2d.setColor(new Color(240, 240, 240));
            g2d.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int y = height - padding - (i * (height - 2 * padding) / 4);
                g2d.drawLine(padding, y, width - padding, y);
            }

            // Draw line and area
            if (values.size() > 1) {
                int xStep = (width - 2 * padding) / Math.max(1, values.size() - 1);

                // Draw gradient area under line
                int[] xPoints = new int[values.size() + 2];
                int[] yPoints = new int[values.size() + 2];
                
                for (int i = 0; i < values.size(); i++) {
                    xPoints[i] = padding + i * xStep;
                    BigDecimal val = values.get(i) != null ? values.get(i) : BigDecimal.ZERO;
                    yPoints[i] = height - padding - (int) ((val.doubleValue() / maxValue.doubleValue()) * (height - 2 * padding));
                }
                
                // Close the polygon
                xPoints[values.size()] = padding + (values.size() - 1) * xStep;
                yPoints[values.size()] = height - padding;
                xPoints[values.size() + 1] = padding;
                yPoints[values.size() + 1] = height - padding;
                
                // Draw gradient fill
                GradientPaint gradient = new GradientPaint(
                    0, padding, 
                    new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 50),
                    0, height - padding,
                    new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 10)
                );
                g2d.setPaint(gradient);
                g2d.fillPolygon(xPoints, yPoints, values.size() + 2);

                // Draw line
                g2d.setColor(lineColor);
                g2d.setStroke(new BasicStroke(3f));

                for (int i = 0; i < values.size() - 1; i++) {
                    BigDecimal val1 = values.get(i) != null ? values.get(i) : BigDecimal.ZERO;
                    BigDecimal val2 = values.get(i + 1) != null ? values.get(i + 1) : BigDecimal.ZERO;
                    
                    int x1 = padding + i * xStep;
                    int y1 = height - padding - (int) ((val1.doubleValue() / maxValue.doubleValue()) * (height - 2 * padding));
                    int x2 = padding + (i + 1) * xStep;
                    int y2 = height - padding - (int) ((val2.doubleValue() / maxValue.doubleValue()) * (height - 2 * padding));

                    g2d.drawLine(x1, y1, x2, y2);
                }
            }

            // Draw points
            g2d.setColor(lineColor);
            int xStep = values.size() > 1 ? (width - 2 * padding) / (values.size() - 1) : 0;
            for (int i = 0; i < values.size(); i++) {
                BigDecimal val = values.get(i) != null ? values.get(i) : BigDecimal.ZERO;
                int x = padding + i * xStep;
                int y = height - padding - (int) ((val.doubleValue() / maxValue.doubleValue()) * (height - 2 * padding));
                
                // Outer circle
                g2d.fillOval(x - 5, y - 5, 10, 10);
                
                // Inner white circle
                g2d.setColor(CARD_BG);
                g2d.fillOval(x - 3, y - 3, 6, 6);
                g2d.setColor(lineColor);
            }

            // Draw labels
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < labels.size(); i++) {
                if (i % 2 == 0 || labels.size() <= 6) {
                    int x = padding + i * xStep;
                    String label = labels.get(i);
                    int labelWidth = g2d.getFontMetrics().stringWidth(label);
                    g2d.drawString(label, x - labelWidth / 2, height - labelPadding + 15);
                }
            }

            // Draw Y-axis labels
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                BigDecimal value = maxValue.multiply(BigDecimal.valueOf(i)).divide(BigDecimal.valueOf(4), 0, java.math.RoundingMode.HALF_UP);
                String label = formatShortCurrency(value);
                int y = height - padding - (i * (height - 2 * padding) / 4);
                g2d.drawString(label, 5, y + 5);
            }
        }

        private String formatShortCurrency(BigDecimal amount) {
            long value = amount.longValue();
            if (value >= 1_000_000_000) {
                return (value / 1_000_000_000) + "B";
            } else if (value >= 1_000_000) {
                return (value / 1_000_000) + "M";
            } else if (value >= 1_000) {
                return (value / 1_000) + "K";
            }
            return String.valueOf(value);
        }
    }

    // ===== INNER CLASS: PIE CHART =====
    private class PieChartPanel extends JPanel {
        private int paid;
        private int unpaid;
        private Color paidColor;
        private Color unpaidColor;

        public PieChartPanel(int paid, int unpaid, Color paidColor, Color unpaidColor) {
            this.paid = paid;
            this.unpaid = unpaid;
            this.paidColor = paidColor;
            this.unpaidColor = unpaidColor;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(500, 280));
            setMinimumSize(new Dimension(400, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int diameter = Math.min(width, height) - 120;
            int x = (width - diameter) / 2;
            int y = 60;

            int total = paid + unpaid;
            
            if (total == 0) {
                // No data - draw gray circle
                g2d.setColor(new Color(220, 220, 220));
                g2d.fillOval(x, y, diameter, diameter);
                
                // Draw border
                g2d.setColor(new Color(180, 180, 180));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawOval(x, y, diameter, diameter);
                
                // Draw message
                g2d.setColor(new Color(100, 100, 100));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "Chưa có dữ liệu";
                int msgWidth = g2d.getFontMetrics().stringWidth(msg);
                g2d.drawString(msg, (width - msgWidth) / 2, y + diameter / 2);
                return;
            }

            // Calculate angles
            double paidPercent = (double) paid / total;
            int paidAngle = (int) Math.round(paidPercent * 360);
            int unpaidAngle = 360 - paidAngle;

            // Draw paid slice
            g2d.setColor(paidColor);
            g2d.fillArc(x, y, diameter, diameter, 90, -paidAngle);

            // Draw unpaid slice
            g2d.setColor(unpaidColor);
            g2d.fillArc(x, y, diameter, diameter, 90 - paidAngle, -unpaidAngle);

            // Draw border around pie
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(x, y, diameter, diameter);

            // Draw legend
            int legendY = y + diameter + 40;
            int legendX = Math.max(20, (width - 250) / 2);

            // Paid legend
            g2d.setColor(paidColor);
            g2d.fillRoundRect(legendX, legendY, 18, 18, 4, 4);
            
            g2d.setColor(new Color(33, 37, 41));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String paidText = String.format("Đã thanh toán: %d (%d%%)", paid, Math.round(paidPercent * 100));
            g2d.drawString(paidText, legendX + 25, legendY + 14);

            // Unpaid legend
            legendY += 30;
            g2d.setColor(unpaidColor);
            g2d.fillRoundRect(legendX, legendY, 18, 18, 4, 4);
            
            g2d.setColor(new Color(33, 37, 41));
            double unpaidPercent = 1 - paidPercent;
            String unpaidText = String.format("Chưa thanh toán: %d (%d%%)", unpaid, Math.round(unpaidPercent * 100));
            g2d.drawString(unpaidText, legendX + 25, legendY + 14);
        }
    }
}