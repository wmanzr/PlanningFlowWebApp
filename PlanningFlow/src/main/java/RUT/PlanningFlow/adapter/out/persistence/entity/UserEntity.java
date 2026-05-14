package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity implements Serializable {

    private String username;

    private String password;

    private String email;

    private String fullName;

    private LocalDate birthDate;

    private List<RoleEntity> roles;

    public UserEntity() {
        this.roles = new ArrayList<>();
    }

    public UserEntity(
            final String username,
            final String password,
            final String email,
            final String fullName,
            final LocalDate birthDate
    ) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.birthDate = birthDate;
    }

    @Column(nullable = false, unique = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    @Column(unique = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @ManyToMany(fetch = FetchType.EAGER)
    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }

    @Column(name = "full_name")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "birth_date", nullable = false)
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

}