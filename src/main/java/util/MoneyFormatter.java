package util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Utility class for formatting money fields
 * Formats: 2000000 → 2.000.000 VNĐ
 */
public class MoneyFormatter {
    
    /**
     * Create a formatted text field for Vietnamese money format
     * Example: 2.000.000 VNĐ
     */
    public static JTextField createMoneyField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                
                // Remove all dots and non-digits
                String digitsOnly = currentText.replace(".", "").replaceAll("[^0-9]", "");
                
                if (digitsOnly.isEmpty()) {
                    super.replace(fb, 0, fb.getDocument().getLength(), "", attrs);
                    return;
                }
                
                // Format with dots
                String formatted = formatWithDots(digitsOnly);
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, attrs);
            }
            
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText = currentText.substring(0, offset) + currentText.substring(offset + length);
                
                String digitsOnly = currentText.replace(".", "").replaceAll("[^0-9]", "");
                
                if (digitsOnly.isEmpty()) {
                    super.remove(fb, 0, fb.getDocument().getLength());
                    return;
                }
                
                String formatted = formatWithDots(digitsOnly);
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, null);
            }
        });
        
        return field;
    }
    
    /**
     * Create a formatted text field with custom size
     */
    public static JTextField createMoneyField(int height) {
        JTextField field = createMoneyField();
        field.setPreferredSize(new Dimension(0, height));
        return field;
    }
    
    /**
     * Format digits with dots
     * Example: "2000000" → "2.000.000"
     */
    private static String formatWithDots(String digitsOnly) {
        StringBuilder result = new StringBuilder();
        int len = digitsOnly.length();
        
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                result.append('.');
            }
            result.append(digitsOnly.charAt(i));
        }
        
        return result.toString();
    }
    
    /**
     * Get numeric value from formatted field
     */
    public static Long getValue(JTextField field) {
        String text = field.getText().replace(".", "").trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Set value to formatted field
     */
    public static void setValue(JTextField field, Long value) {
        if (value == null) {
            field.setText("");
        } else {
            field.setText(formatWithDots(String.valueOf(value)));
        }
    }
    
    /**
     * Format long value to Vietnamese money string
     * Example: 2000000 → "2.000.000"
     */
    public static String formatMoney(long value) {
        return formatWithDots(String.valueOf(value));
    }
    
    /**
     * Format BigDecimal to Vietnamese money string
     */
    public static String formatMoney(java.math.BigDecimal value) {
        if (value == null) return "0";
        return formatMoney(value.longValue());
    }
    
    /**
     * Parse formatted money string to long
     * Example: "2.000.000" → 2000000
     */
    public static long parseMoney(String formattedValue) {
        if (formattedValue == null || formattedValue.trim().isEmpty()) {
            return 0;
        }
        
        String cleaned = formattedValue.replace(".", "").replace(",", "").trim();
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}