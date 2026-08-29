package helpdesk;

import java.util.ArrayList;

public abstract class Customer implements Identifiable {
    private final String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private final ArrayList<Product> products;
    private final ArrayList<Ticket> ticketHistory;

    protected Customer(String id, String name, String email, String phone, String address) {
        this.id = requireText(id, "Customer ID");
        products = new ArrayList<Product>();
        ticketHistory = new ArrayList<Ticket>();
        updateDetails(name, email, phone, address);
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private static String requireEmail(String value) {
        String checkedEmail = requireText(value, "Email");
        if (!checkedEmail.contains("@")) {
            throw new IllegalArgumentException("Email must contain @.");
        }
        return checkedEmail;
    }

    public abstract CustomerCategory getCategory();

    public abstract TreatmentPriority getTreatmentPriority();

    public abstract String getSupportPolicy();

    public abstract String getCategorySpecificInformation();

    public abstract void updateCategoryInformation(String firstValue, String secondValue);

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public final void updateDetails(String name, String email, String phone, String address) {
        this.name = requireText(name, "Customer name");
        this.email = requireEmail(email);
        this.phone = requireText(phone, "Phone");
        this.address = requireText(address, "Address");
    }

    public ArrayList<Product> getProducts() {
        return new ArrayList<Product>(products);
    }

    public ArrayList<Ticket> getTicketHistory() {
        return new ArrayList<Ticket>(ticketHistory);
    }

    void addProduct(Product product) throws HelpDeskException {
        if (product == null) {
            throw new HelpDeskException("A null product cannot be assigned.");
        }
        if (hasProduct(product.getId())) {
            throw new HelpDeskException(getName() + " already has product " + product.getId() + ".");
        }
        products.add(product);
    }

    void addTicketToHistory(Ticket ticket) {
        if (ticket != null && !ticketHistory.contains(ticket)) {
            ticketHistory.add(ticket);
        }
    }

    public boolean hasProduct(String productId) {
        if (productId == null) {
            return false;
        }
        for (Product product : products) {
            if (product.getId().equalsIgnoreCase(productId.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        String value = keyword.trim().toLowerCase();
        return id.toLowerCase().contains(value)
                || name.toLowerCase().contains(value)
                || email.toLowerCase().contains(value)
                || phone.toLowerCase().contains(value)
                || address.toLowerCase().contains(value)
                || getCategorySpecificInformation().toLowerCase().contains(value);
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + getCategory() + " | priority: "
                + getTreatmentPriority() + " | products: " + products.size();
    }
}
