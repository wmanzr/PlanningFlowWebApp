package RUT.PlanningFlow.application.pagination;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long totalElements,
        int totalPages
) {
    public PageResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}