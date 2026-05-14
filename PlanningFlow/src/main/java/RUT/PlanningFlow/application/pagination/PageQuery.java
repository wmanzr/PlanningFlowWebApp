package RUT.PlanningFlow.application.pagination;

import RUT.PlanningFlow.domain.utils.DomainAssert;

public record PageQuery(
        int page,
        int size
) {
    public PageQuery {
        DomainAssert.isTrue(page >= 1, "Номер страницы должен быть >= 1", "INVALID_PAGE_NUMBER");
        DomainAssert.isTrue(size > 0, "Размер страницы должен быть положительным", "INVALID_PAGE_SIZE");
    }

    public int zeroBasedPage() {
        return page - 1;
    }

    public int offset() {
        return zeroBasedPage() * size;
    }
}

