package helpdesk;

public class BusinessCustomer extends Customer {
    private String companyRegistrationNumber;
    private String contactPerson;

    public BusinessCustomer(String id, String name, String email, String phone, String address,
            String companyRegistrationNumber, String contactPerson) {
        super(id, name, email, phone, address);
        this.companyRegistrationNumber = requireText(companyRegistrationNumber,
                "Company registration number");
        this.contactPerson = requireText(contactPerson, "Contact person");
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    @Override
    public CustomerCategory getCategory() {
        return CustomerCategory.BUSINESS;
    }

    @Override
    public TreatmentPriority getTreatmentPriority() {
        return TreatmentPriority.HIGH;
    }

    @Override
    public String getSupportPolicy() {
        return "High-priority support and access to business connectivity offers.";
    }

    @Override
    public String getCategorySpecificInformation() {
        return "Registration no.: " + companyRegistrationNumber + ", contact: " + contactPerson;
    }

    @Override
    public void updateCategoryInformation(String firstValue, String secondValue) {
        companyRegistrationNumber = requireText(firstValue, "Company registration number");
        contactPerson = requireText(secondValue, "Contact person");
    }
}
