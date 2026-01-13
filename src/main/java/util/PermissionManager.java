package util;

import model.User;

/**
 * Permission Manager - RBAC (Role-Based Access Control)
 * Manages access control for 3 roles: ADMIN, STAFF, ACCOUNTANT
 */
public class PermissionManager {
    
    // Module identifiers
    public static final String MODULE_DASHBOARD = "DASHBOARD";
    public static final String MODULE_BUILDINGS = "BUILDINGS";
    public static final String MODULE_FLOORS = "FLOORS";
    public static final String MODULE_APARTMENTS = "APARTMENTS";
    public static final String MODULE_RESIDENTS = "RESIDENTS";
    public static final String MODULE_CONTRACTS = "CONTRACTS";
    public static final String MODULE_SERVICES = "SERVICES";
    public static final String MODULE_INVOICES = "INVOICES";
    public static final String MODULE_REPORTS = "REPORTS";
    
    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    
    private static PermissionManager instance;
    
    private PermissionManager() {
    }
    
    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }
    
    /**
     * Get current user from session
     */
    private User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }
    
    /**
     * Get current user role
     */
    private String getCurrentRole() {
        User user = getCurrentUser();
        return (user != null) ? user.getRole() : null;
    }
    
    // ==================== ACCESS CONTROL ====================
    
    /**
     * Check if current user can access a module
     */
    public boolean canAccess(String module) {
        String role = getCurrentRole();
        if (role == null) return false;
        
        // ADMIN has full access
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return true;
        }
        
        // STAFF permissions
        if (ROLE_STAFF.equalsIgnoreCase(role)) {
            switch (module) {
                case MODULE_DASHBOARD:
                case MODULE_BUILDINGS:   // View only
                case MODULE_FLOORS:      // View only
                case MODULE_APARTMENTS:  // CRUD
                case MODULE_RESIDENTS:   // CRUD
                case MODULE_CONTRACTS:   // CRUD
                case MODULE_SERVICES:    // View only
                    return true;
                case MODULE_INVOICES:
                case MODULE_REPORTS:
                    return false;
                default:
                    return false;
            }
        }
        
        // ACCOUNTANT permissions
        if (ROLE_ACCOUNTANT.equalsIgnoreCase(role)) {
            switch (module) {
                case MODULE_DASHBOARD:
                case MODULE_BUILDINGS:   // View only
                case MODULE_APARTMENTS:  // View only
                case MODULE_RESIDENTS:   // View only
                case MODULE_CONTRACTS:   // View only
                case MODULE_SERVICES:    // CRUD
                case MODULE_INVOICES:    // CRUD
                case MODULE_REPORTS:     // View only
                    return true;
                case MODULE_FLOORS:
                    return false;
                default:
                    return false;
            }
        }
        
        return false;
    }
    
    /**
     * Check if current user can view a module
     */
    public boolean canView(String module) {
        return canAccess(module);
    }
    
    /**
     * Check if current user can add to a module
     */
    public boolean canAdd(String module) {
        String role = getCurrentRole();
        if (role == null) return false;
        
        // ADMIN can add to everything
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return true;
        }
        
        // STAFF can add to: Apartments, Residents, Contracts
        if (ROLE_STAFF.equalsIgnoreCase(role)) {
            return module.equals(MODULE_APARTMENTS) ||
                   module.equals(MODULE_RESIDENTS) ||
                   module.equals(MODULE_CONTRACTS);
        }
        
        // ACCOUNTANT can add to: Services, Invoices
        if (ROLE_ACCOUNTANT.equalsIgnoreCase(role)) {
            return module.equals(MODULE_SERVICES) ||
                   module.equals(MODULE_INVOICES);
        }
        
        return false;
    }
    
    /**
     * Check if current user can edit in a module
     */
    public boolean canEdit(String module) {
        // Same as canAdd for this implementation
        return canAdd(module);
    }
    
    /**
     * Check if current user can delete from a module
     */
    public boolean canDelete(String module) {
        // Same as canAdd for this implementation
        return canAdd(module);
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if current user is ADMIN
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(getCurrentRole());
    }
    
    /**
     * Check if current user is STAFF
     */
    public boolean isStaff() {
        return ROLE_STAFF.equalsIgnoreCase(getCurrentRole());
    }
    
    /**
     * Check if current user is ACCOUNTANT
     */
    public boolean isAccountant() {
        return ROLE_ACCOUNTANT.equalsIgnoreCase(getCurrentRole());
    }
    
    /**
     * Get permission description for a module
     */
    public String getPermissionDescription(String module) {
        String role = getCurrentRole();
        if (role == null) return "No access";
        
        if (isAdmin()) {
            return "Full access (CRUD)";
        }
        
        if (canAdd(module)) {
            return "Full access (CRUD)";
        }
        
        if (canView(module)) {
            return "View only";
        }
        
        return "No access";
    }
    
    /**
     * Show access denied message
     */
    public void showAccessDeniedMessage(java.awt.Component parent, String action) {
        javax.swing.JOptionPane.showMessageDialog(parent,
            "Bạn không có quyền " + action + "!\n" +
            "Vai trò: " + getCurrentRole(),
            "Không Có Quyền Truy Cập",
            javax.swing.JOptionPane.WARNING_MESSAGE);
    }
    
    // ==================== PERMISSION SUMMARY ====================
    
    /**
     * Get permission summary for current user (for debugging)
     */
    public String getPermissionSummary() {
        String role = getCurrentRole();
        if (role == null) return "Not logged in";
        
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(role).append("\n\n");
        sb.append("Permissions:\n");
        
        String[] modules = {
            MODULE_DASHBOARD, MODULE_BUILDINGS, MODULE_FLOORS, MODULE_APARTMENTS,
            MODULE_RESIDENTS, MODULE_CONTRACTS, MODULE_SERVICES, MODULE_INVOICES, MODULE_REPORTS
        };
        
        for (String module : modules) {
            sb.append("- ").append(module).append(": ");
            if (!canAccess(module)) {
                sb.append("No access");
            } else if (canAdd(module)) {
                sb.append("CRUD (View, Add, Edit, Delete)");
            } else {
                sb.append("View only");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}