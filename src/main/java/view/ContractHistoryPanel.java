package view;

import dao.ContractHistoryDAO;
import model.ContractHistory;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * COMPLETELY NEW DESIGN - Modern Card-Based Timeline
 * Full information display, clear hierarchy, professional look
 */
public class ContractHistoryPanel extends JPanel {
    
    private ContractHistoryDAO historyDAO;
    private Long contractId;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy 'lúc' HH:mm");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    private JPanel timelinePanel;
    private JLabel countLabel;
    
    public ContractHistoryPanel(Long contractId) {
        this.contractId = contractId;
        this.historyDAO = new ContractHistoryDAO();
        
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        initComponents();
        loadHistory();
    }
    
    private void initComponents() {
        add(createModernHeader(), BorderLayout.NORTH);
        
        timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        timelinePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createModernHeader() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(99, 102, 241),
                    0, getHeight(), new Color(139, 92, 246)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Left: Icon + Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        // Clock icon
        JPanel clockIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background circle
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillOval(0, 0, 50, 50);
                
                // Clock face
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawOval(10, 10, 30, 30);
                
                // Clock hands
                g2d.drawLine(25, 25, 25, 17); // Hour
                g2d.drawLine(25, 25, 32, 25); // Minute
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }
        };
        clockIcon.setOpaque(false);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Lịch Sử Thay Đổi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Theo dõi mọi hoạt động của hợp đồng");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);
        
        leftPanel.add(clockIcon);
        leftPanel.add(textPanel);
        
        // Right: Count badge
        countLabel = new JLabel("0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countLabel.setForeground(Color.WHITE);
        
        JLabel countText = new JLabel("thay đổi");
        countText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countText.setForeground(new Color(255, 255, 255, 180));
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(countLabel);
        rightPanel.add(countText);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadHistory() {
        timelinePanel.removeAll();
        
        List<ContractHistory> histories = historyDAO.getHistoryByContractWithUser(contractId);
        
        if (histories.isEmpty()) {
            timelinePanel.add(createModernEmptyState());
            countLabel.setText("0");
        } else {
            for (int i = 0; i < histories.size(); i++) {
                ContractHistory history = histories.get(i);
                boolean isLast = (i == histories.size() - 1);
                
                timelinePanel.add(createModernHistoryCard(history, i + 1, histories.size()));
                
                if (!isLast) {
                    timelinePanel.add(Box.createVerticalStrut(16));
                }
            }
            
            countLabel.setText(String.valueOf(histories.size()));
        }
        
        timelinePanel.revalidate();
        timelinePanel.repaint();
    }
    
    private JPanel createModernEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 4));
                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 16, 16);
                
                // Background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Dashed border
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
                    0, new float[]{10, 5}, 0));
                g2d.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        panel.setBorder(new EmptyBorder(50, 30, 50, 30));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        
        JLabel icon = new JLabel("📋");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title = new JLabel("Chưa Có Lịch Sử");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(75, 85, 99));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel desc = new JLabel("Các thay đổi của hợp đồng sẽ được ghi lại tại đây");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(new Color(156, 163, 175));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(icon);
        content.add(Box.createVerticalStrut(20));
        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(desc);
        
        panel.add(content);
        return panel;
    }
    
    private JPanel createModernHistoryCard(ContractHistory history, int index, int total) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 6));
                g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 16, 16);
                
                // White background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Top colored stripe
                Color stripeColor = getActionColor(history.getAction());
                g2d.setColor(stripeColor);
                g2d.fillRoundRect(0, 0, getWidth(), 6, 16, 16);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        card.setLayout(new BorderLayout(0, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        
        // Main content area
        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        
        // Left: Number badge + Icon
        JPanel leftSection = createLeftSection(history, index);
        
        // Center: All information
        JPanel centerSection = createCenterSection(history);
        
        // Right: User info
        JPanel rightSection = createRightSection(history);
        
        mainContent.add(leftSection, BorderLayout.WEST);
        mainContent.add(centerSection, BorderLayout.CENTER);
        mainContent.add(rightSection, BorderLayout.EAST);
        
        card.add(mainContent, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createLeftSection(ContractHistory history, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(70, 140));
        
        // Number badge
        JLabel numberLabel = new JLabel("#" + index);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        numberLabel.setForeground(new Color(107, 114, 128));
        numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(numberLabel);
        panel.add(Box.createVerticalStrut(12));
        
        // Icon circle
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color = getActionColor(history.getAction());
                
                // Outer circle
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2d.fillOval(0, 0, 60, 60);
                
                // Inner circle
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
                g2d.fillOval(10, 10, 40, 40);
            }
        };
        iconCircle.setPreferredSize(new Dimension(60, 60));
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout());
        
        JLabel iconLabel = new JLabel(getActionIcon(history.getAction()));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconCircle.add(iconLabel);
        
        panel.add(iconCircle);
        
        return panel;
    }
    
    private JPanel createCenterSection(ContractHistory history) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Action title + badge
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        
        JLabel actionTitle = new JLabel(history.getActionDisplay());
        actionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionTitle.setForeground(new Color(17, 24, 39));
        
        titleRow.add(actionTitle);
        titleRow.add(Box.createHorizontalStrut(12));
        
        // Status badge
        JLabel statusBadge = new JLabel(getActionLabel(history.getAction()));
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(getActionColor(history.getAction()));
        statusBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        
        titleRow.add(statusBadge);
        
        panel.add(titleRow);
        panel.add(Box.createVerticalStrut(10));
        
        // Date/Time with icon
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateRow.setOpaque(false);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        
        JLabel clockIcon = new JLabel("🕐");
        clockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        JLabel dateLabel = new JLabel(dateTimeFormat.format(history.getCreatedAt()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(107, 114, 128));
        
        dateRow.add(clockIcon);
        dateRow.add(Box.createHorizontalStrut(6));
        dateRow.add(dateLabel);
        
        panel.add(dateRow);
        panel.add(Box.createVerticalStrut(12));
        
        // Description/Details
        String desc = getDescription(history);
        if (desc != null && !desc.trim().isEmpty()) {
            JPanel descPanel = new JPanel(new BorderLayout());
            descPanel.setOpaque(true);
            descPanel.setBackground(new Color(249, 250, 251));
            descPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(12, 14, 12, 14)
            ));
            descPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            
            JLabel descLabel = new JLabel("<html>" + desc + "</html>");
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descLabel.setForeground(new Color(55, 65, 81));
            
            descPanel.add(descLabel);
            panel.add(descPanel);
        }
        
        return panel;
    }
    
    private JPanel createRightSection(ContractHistory history) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(120, 140));
        
        // User avatar circle
        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(99, 102, 241),
                    50, 50, new Color(139, 92, 246)
                );
                g2d.setPaint(gradient);
                g2d.fillOval(0, 0, 50, 50);
                
                // User icon
                g2d.setColor(Color.WHITE);
                // Head
                g2d.fillOval(18, 12, 14, 14);
                // Body
                g2d.fillArc(13, 26, 24, 20, 0, -180);
            }
        };
        avatarCircle.setPreferredSize(new Dimension(50, 50));
        avatarCircle.setOpaque(false);
        avatarCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(avatarCircle);
        panel.add(Box.createVerticalStrut(10));
        
        // User name
        String userName = history.getCreatedByName() != null ? history.getCreatedByName() : "Hệ thống";
        JLabel nameLabel = new JLabel("<html><center>" + userName + "</center></html>");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(17, 24, 39));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(nameLabel);
        
        return panel;
    }
    
        private String getDescription(ContractHistory history) {
            String desc = history.getReason();

            if (desc == null) {
                desc = "";
            }

            // ✅ Add contract type info for CREATED action
            if ("CREATED".equals(history.getAction())) {
                // Could fetch contract type from database if needed
                // For now, just use existing reason
            }

            // Add date range for renewals (only for RENTAL)
            if (history.getOldEndDate() != null && history.getNewEndDate() != null) {
                String dateInfo = "<b>Thời gian gia hạn:</b><br>" +
                                "• Từ ngày: " + dateFormat.format(history.getOldEndDate()) + "<br>" +
                                "• Đến ngày: " + dateFormat.format(history.getNewEndDate());

                desc = desc.isEmpty() ? dateInfo : desc + "<br><br>" + dateInfo;
            }

            return desc;
        }
    
    private String getActionIcon(String action) {
        if (action == null) return "📝";
        
        switch (action.toUpperCase()) {
            case "CREATED":
                return "✨";
            case "RENEWED":
            case "EXTENDED":
                return "🔄";
            case "UPDATED":
                return "✏️";
            case "TERMINATED":
                return "❌";
            case "DELETED":
                return "🗑️";
            case "STATUS_CHANGED":
                return "🔀";
            default:
                return "📝";
        }
    }
    
    private String getActionLabel(String action) {
        if (action == null) return "KHÁC";
        
        switch (action.toUpperCase()) {
            case "CREATED":
                return "TẠO MỚI";
            case "RENEWED":
            case "EXTENDED":
                return "GIA HẠN";
            case "UPDATED":
                return "CẬP NHẬT";
            case "TERMINATED":
                return "KẾT THÚC";
            case "DELETED":
                return "XÓA";
            case "STATUS_CHANGED":
                return "THAY ĐỔI";
            default:
                return "KHÁC";
        }
    }
    
    private Color getActionColor(String action) {
        if (action == null) return new Color(107, 114, 128);
        
        switch (action.toUpperCase()) {
            case "CREATED":
                return new Color(16, 185, 129); // Emerald
            case "RENEWED":
            case "EXTENDED":
                return new Color(59, 130, 246); // Blue
            case "UPDATED":
                return new Color(245, 158, 11); // Amber
            case "TERMINATED":
            case "DELETED":
                return new Color(239, 68, 68); // Red
            case "STATUS_CHANGED":
                return new Color(139, 92, 246); // Violet
            default:
                return new Color(107, 114, 128);
        }
    }
    
    public void refresh() {
        loadHistory();
    }
}