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
 * Modern Timeline Design for Contract History
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
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Timeline
        timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Left: Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Lịch Sử Thay Đổi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        leftPanel.add(titleLabel);
        
        // Right: Count
        countLabel = new JLabel("0 thay đổi");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadHistory() {
        timelinePanel.removeAll();
        
        List<ContractHistory> histories = historyDAO.getHistoryByContractWithUser(contractId);
        
        if (histories.isEmpty()) {
            timelinePanel.add(createEmptyState());
            countLabel.setText("0 thay đổi");
        } else {
            for (int i = 0; i < histories.size(); i++) {
                ContractHistory history = histories.get(i);
                boolean isLast = (i == histories.size() - 1);
                timelinePanel.add(createTimelineItem(history, isLast));
            }
            
            countLabel.setText(histories.size() + " thay đổi");
        }
        
        timelinePanel.revalidate();
        timelinePanel.repaint();
    }
    
    private JPanel createEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(0, 350));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel("📋");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel textLabel = new JLabel("Chưa có lịch sử");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textLabel.setForeground(UIConstants.TEXT_SECONDARY);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Các thay đổi sẽ được ghi lại tại đây");
        subLabel.setFont(UIConstants.FONT_SMALL);
        subLabel.setForeground(new Color(156, 163, 175));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(textLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(subLabel);
        
        panel.add(content);
        return panel;
    }
    
    private JPanel createTimelineItem(ContractHistory history, boolean isLast) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(0, 25, 0, 25));
        
        // Left: Indicator
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(50, 0));
        
        // Dot
        JPanel dotContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        dotContainer.setBackground(Color.WHITE);
        
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setBackground(getActionColor(history.getAction()));
        dot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3),
            BorderFactory.createLineBorder(getActionColor(history.getAction()), 2)
        ));
        
        dotContainer.add(dot);
        leftPanel.add(dotContainer, BorderLayout.NORTH);
        
        // Line
        if (!isLast) {
            JPanel line = new JPanel();
            line.setBackground(new Color(229, 231, 235));
            line.setPreferredSize(new Dimension(2, 0));
            
            JPanel lineContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            lineContainer.setBackground(Color.WHITE);
            lineContainer.add(line);
            
            leftPanel.add(lineContainer, BorderLayout.CENTER);
        }
        
        // Right: Content
        JPanel contentPanel = createContent(history);
        
        wrapper.add(leftPanel, BorderLayout.WEST);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JPanel createContent(ContractHistory history) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 10, 25, 0));
        
        // Row 1: Date & User
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(Color.WHITE);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        JLabel dateLabel = new JLabel(dateTimeFormat.format(history.getCreatedAt()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        JLabel userLabel = new JLabel(history.getCreatedByName() != null ? history.getCreatedByName() : "Hệ thống");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        row1.add(dateLabel, BorderLayout.WEST);
        row1.add(userLabel, BorderLayout.EAST);
        
        panel.add(row1);
        panel.add(Box.createVerticalStrut(8));
        
        // Row 2: Action badge
        panel.add(createActionBadge(history));
        
        // Row 3: Description (if exists)
        String desc = getDescription(history);
        if (desc != null && !desc.trim().isEmpty()) {
            panel.add(Box.createVerticalStrut(10));
            panel.add(createDescriptionPanel(desc));
        }
        
        return panel;
    }
    
    private JPanel createActionBadge(ContractHistory history) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        
        Color actionColor = getActionColor(history.getAction());
        badge.setBackground(actionColor);
        badge.setOpaque(true);  // ← CRITICAL: Makes background visible
        badge.setBorder(BorderFactory.createEmptyBorder());
        
        JLabel label = new JLabel(getActionIcon(history.getAction()) + "  " + history.getActionDisplay());
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        label.setOpaque(false);  // ← Label transparent để thấy background của panel
        
        badge.add(label);
        wrapper.add(badge);
        
        return wrapper;
    }
    
    private JPanel createDescriptionPanel(String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        JLabel label = new JLabel("<html>" + description + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(55, 65, 81));
        
        panel.add(label, BorderLayout.CENTER);
        
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