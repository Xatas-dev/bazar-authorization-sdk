package org.bazar.authorization.sdk;

/**
 * Enum representing all available permissions combining resource and action.
 * Each permission is a combination of a resource (e.g., space_user) and an action (e.g., ADD).
 * Example: SPACE_USER_ADD, CHAT_MESSAGES_READ, SPACE_WRITE
 */
public enum Permission {
    // SPACE permissions
    SPACE_WRITE("space", "WRITE"),
    SPACE_DELETE("space", "DELETE"),

    // CHAT_MESSAGES permissions
    CHAT_MESSAGES_READ("chat_messages", "READ"),
    CHAT_MESSAGES_WRITE("chat_messages", "WRITE"),
    CHAT_MESSAGES_DELETE("chat_messages", "DELETE"),

    // SPACE_USER permissions
    SPACE_USER_DELETE("space_user", "DELETE"),
    SPACE_USER_ADD("space_user", "ADD"),

    // SPACE_USER_ACTIONS permissions
    SPACE_USER_ACTIONS_READ("space_user_actions", "READ"),
    SPACE_USER_ACTIONS_WRITE("space_user_actions", "WRITE");

    private final String resource;
    private final String action;

    Permission(String resource, String action) {
        this.resource = resource;
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    /**
     * Get Permission from resource and action codes
     */
    public static Permission from(String resource, String action) {
        for (Permission permission : Permission.values()) {
            if (permission.resource.equals(resource) && permission.action.equals(action)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission: resource=" + resource + ", action=" + action);
    }

    /**
     * Get Permission from combined code like "space_user.ADD" or "space_user_ADD"
     */
    public static Permission fromCode(String code) {
        try {
            return Permission.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown permission code: " + code);
        }
    }
}

