package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "skills")
public class SkillEntity extends BaseEntity implements Serializable {
    private String name;
    private String category;

    public SkillEntity() {}

    public SkillEntity(String name, String category) {
        this.name = name;
        this.category = category;
    }

    @Column(nullable = false, unique = true)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Column(name = "category", nullable = false)
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}