package helpdesk;

public class ResidentialCustomer extends Customer {
    private String personalNumber;

    public ResidentialCustomer(String id, String name, String email, String phone, String address,
            String personalNumber) {
        super(id, name, email, phone, address);
        this.personalNumber = requireText(personalNumber, "Personal number");
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    @Override
    public CustomerCategory getCategory() {
        return CustomerCategory.RESIDENTIAL;
    }

    @Override
    public TreatmentPriority getTreatmentPriority() {
        return TreatmentPriority.STANDARD;
    }

    @Override
    public String getSupportPolicy() {
        return "Standard support and access to residential internet and equipment offers.";
    }

    @Override
    public String getCategorySpecificInformation() {
        return "Personal number: " + personalNumber;
    }

    @Override
    public void updateCategoryInformation(String firstValue, String secondValue) {
        personalNumber = requireText(firstValue, "Personal number");
    }
}
