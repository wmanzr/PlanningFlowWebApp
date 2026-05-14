package RUT.PlanningFlow.adapter.out.persistence.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {
    private int id;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}