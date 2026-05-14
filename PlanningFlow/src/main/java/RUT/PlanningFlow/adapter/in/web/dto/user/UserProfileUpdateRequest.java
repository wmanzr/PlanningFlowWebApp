package RUT.PlanningFlow.adapter.in.web.dto.user;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(max = 512, message = "ФИО слишком длинное")
    private String fullName;

    public UserProfileUpdateRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }
}
