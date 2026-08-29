package helpdesk;

import java.util.ArrayList;

public class AuthenticationService {
    private final ArrayList<UserAccount> accounts;

    public AuthenticationService() {
        accounts = new ArrayList<UserAccount>();
    }

    public void addAccount(UserAccount account) throws HelpDeskException {
        if (account == null) {
            throw new IllegalArgumentException("User account is required.");
        }
        for (UserAccount existing : accounts) {
            if (existing.getUsername().equalsIgnoreCase(account.getUsername())) {
                throw new HelpDeskException(
                        "An account with username " + account.getUsername() + " already exists.");
            }
        }
        accounts.add(account);
    }

    public UserAccount authenticate(String username, String password) throws HelpDeskException {
        for (UserAccount account : accounts) {
            if (account.matchesCredentials(username, password)) {
                return account;
            }
        }
        throw new HelpDeskException("Invalid username or password.");
    }

    public ArrayList<UserAccount> getAccounts() {
        return new ArrayList<UserAccount>(accounts);
    }
}
