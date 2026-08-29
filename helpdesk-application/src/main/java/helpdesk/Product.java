package helpdesk;

import java.util.ArrayList;

public class Product implements Identifiable {
    private final String id;
    private final String name;
    private final String description;
    private final ProductType type;
    private final double monthlyPrice;
    private final ArrayList<CustomerCategory> eligibleCategories;

    public Product(String id, String name, String description, ProductType type,
            double monthlyPrice, CustomerCategory[] categories) {
        this.id = requireText(id, "Product ID");
        this.name = requireText(name, "Product name");
        this.description = requireText(description, "Product description");
        if (type == null) {
            throw new IllegalArgumentException("Product type is required.");
        }
        if (monthlyPrice < 0) {
            throw new IllegalArgumentException("Monthly price cannot be negative.");
        }
        if (categories == null || categories.length == 0) {
            throw new IllegalArgumentException("At least one eligible customer category is required.");
        }

        this.type = type;
        this.monthlyPrice = monthlyPrice;
        eligibleCategories = new ArrayList<CustomerCategory>();
        for (CustomerCategory category : categories) {
            if (category != null && !eligibleCategories.contains(category)) {
                eligibleCategories.add(category);
            }
        }
        if (eligibleCategories.isEmpty()) {
            throw new IllegalArgumentException("At least one valid customer category is required.");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductType getType() {
        return type;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public ArrayList<CustomerCategory> getEligibleCategories() {
        return new ArrayList<CustomerCategory>(eligibleCategories);
    }

    public boolean isAvailableTo(CustomerCategory category) {
        return eligibleCategories.contains(category);
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + type + " | EUR "
                + String.format("%.2f", monthlyPrice) + "/month";
    }
}
