package helpdesk;

public class UserAccount {
    private final String username;
    private final String password;
    private final UserRole role;
    private final String linkedEntityId;

    public UserAccount(String username, String password, UserRole role, String linkedEntityId) {
        this.username = requireText(username, "Username");
        this.password = requireText(password, "Password");
        if (role == null) {
            throw new IllegalArgumentException("User role is required.");
        }
        if (role != UserRole.ADMIN
                && (linkedEntityId == null || linkedEntityId.trim().isEmpty())) {
            throw new IllegalArgumentException("Agent and customer accounts require a linked ID.");
        }
        this.role = role;
        this.linkedEntityId = linkedEntityId == null ? null : linkedEntityId.trim();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public String getLinkedEntityId() {
        return linkedEntityId;
    }

    public boolean matchesCredentials(String enteredUsername, String enteredPassword) {
        return enteredUsername != null && enteredPassword != null
                && username.equalsIgnoreCase(enteredUsername.trim())
                && password.equals(enteredPassword);
    }
}
