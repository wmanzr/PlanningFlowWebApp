package RUT.PlanningFlow.domain.model;

import RUT.PlanningFlow.domain.exception.DomainException;
import RUT.PlanningFlow.domain.utils.DomainAssert;

public class Skill {
    private final Integer id;
    private String name;
    private String category;

    public Skill(final Integer id, final String name, final String category) {
        this.id = id;
        final String normalizedName = normalizeCatalogText(name, "Название навыка обязательно", "SKILL_NAME_REQUIRED");
        final String normalizedCategory = normalizeCatalogText(category, "Категория навыка обязательна", "SKILL_CATEGORY_REQUIRED");
        this.name = normalizedName;
        this.category = normalizedCategory;
    }

    public static void assertCatalogNameNotTaken(final boolean nameAlreadyExists) {
        if (nameAlreadyExists) {
            throw new DomainException("Навык с таким названием уже есть в каталоге", "SKILL_NAME_ALREADY_EXISTS");
        }
    }

    public void rename(final String newName) {
        this.name = normalizeCatalogText(newName, "Название навыка обязательно", "SKILL_NAME_REQUIRED");
    }

    public void changeCategory(final String newCategory) {
        this.category = normalizeCatalogText(newCategory, "Категория навыка обязательна", "SKILL_CATEGORY_REQUIRED");
    }

    private static String normalizeCatalogText(final String value, final String blankMessage, final String blankCode) {
        final String trimmed = value == null ? "" : value.trim();
        DomainAssert.notBlank(trimmed, blankMessage, blankCode);
        return trimmed;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Skill that = (Skill) o;
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}