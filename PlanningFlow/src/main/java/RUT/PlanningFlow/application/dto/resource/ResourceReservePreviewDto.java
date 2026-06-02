package RUT.PlanningFlow.application.dto.resource;

/** Сколько единиц закроется с внутреннего склада и сколько уйдёт к внешнему поставщику (как при allocate, без записи в БД). */
public record ResourceReservePreviewDto(int fromInternal, int fromExternal) {
}
