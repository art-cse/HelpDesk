package helpdesk;

public class OfficialCustomer extends Customer {
    private String institutionCode;
    private String department;

    public OfficialCustomer(String id, String name, String email, String phone, String address,
            String institutionCode, String department) {
        super(id, name, email, phone, address);
        this.institutionCode = requireText(institutionCode, "Institution code");
        this.department = requireText(department, "Department");
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public CustomerCategory getCategory() {
        return CustomerCategory.OFFICIAL;
    }

    @Override
    public TreatmentPriority getTreatmentPriority() {
        return TreatmentPriority.URGENT;
    }

    @Override
    public String getSupportPolicy() {
        return "Urgent treatment for public services and access to institutional security offers.";
    }

    @Override
    public String getCategorySpecificInformation() {
        return "Institution code: " + institutionCode + ", department: " + department;
    }

    @Override
    public void updateCategoryInformation(String firstValue, String secondValue) {
        institutionCode = requireText(firstValue, "Institution code");
        department = requireText(secondValue, "Department");
    }
}
