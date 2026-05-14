package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.utils.DomainAssert;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final Integer id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private LocalDate birthDate;
    private final List<Role> roles;

    public User(
            final Integer id,
            final String username,
            final String password,
            final String email,
            final String fullName,
            final LocalDate birthDate,
            final List<Role> roles
    ) {
        this.id = id;
        DomainAssert.notBlank(username, "Имя пользователя обязательно", "USERNAME_REQUIRED");
        DomainAssert.notBlank(password, "Пароль обязателен", "PASSWORD_REQUIRED");
        DomainAssert.notBlank(email, "Email обязателен", "EMAIL_REQUIRED");
        DomainAssert.notBlank(fullName, "ФИО обязательно", "FULL_NAME_REQUIRED");
        DomainAssert.notNull(birthDate, "Дата рождения обязательна", "BIRTH_DATE_REQUIRED");
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        validateBirthDate(birthDate);
        this.birthDate = birthDate;
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }

    private static void validateBirthDate(final LocalDate birthDate) {
        final LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new DomainException("Дата рождения не может быть в будущем", "INVALID_BIRTH_DATE");
        }
        final int years = Period.between(birthDate, today).getYears();
        if (years < 14) {
            throw new DomainException("Регистрация доступна с 14 лет", "INVALID_USER_AGE");
        }
        if (years > 120) {
            throw new DomainException("Указана некорректная дата рождения", "INVALID_BIRTH_DATE");
        }
    }

    public void rename(final String newFullName) {
        DomainAssert.notBlank(newFullName, "ФИО обязательно", "FULL_NAME_REQUIRED");
        this.fullName = newFullName;
    }

    public void changeEmail(final String newEmail) {
        DomainAssert.notBlank(newEmail, "Email обязателен", "EMAIL_REQUIRED");
        this.email = newEmail;
    }

    public void changePassword(final String newPassword) {
        DomainAssert.notBlank(newPassword, "Пароль обязателен", "PASSWORD_REQUIRED");
        this.password = newPassword;
    }

    public void addRole(final Role role) {
        DomainAssert.notNull(role, "Роль обязательна", "ROLE_REQUIRED");
        final boolean exists = roles.stream().anyMatch(existing -> existing.getName() == role.getName());
        if (exists) {
            return;
        }
        roles.add(role);
    }

    public void removeRole(final Role role) {
        DomainAssert.notNull(role, "Роль обязательна", "ROLE_REQUIRED");
        roles.removeIf(existing -> existing.getName() == role.getName());
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<Role> getRoles() { return Collections.unmodifiableList(roles); }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final User that = (User) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
