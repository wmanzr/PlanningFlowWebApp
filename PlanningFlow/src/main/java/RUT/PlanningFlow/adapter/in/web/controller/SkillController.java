package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.adapter.in.web.dto.skill.SkillCreateRequest;
import RUT.PlanningFlow.application.dto.skill.SkillResponseDto;
import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.application.port.in.skill.CreateSkillUseCase;
import RUT.PlanningFlow.application.port.in.skill.DeleteSkillUseCase;
import RUT.PlanningFlow.application.port.in.skill.GetSkillDetailsQuery;
import RUT.PlanningFlow.application.port.in.skill.ListSkillCategoriesQuery;
import RUT.PlanningFlow.application.port.in.skill.ListSkillsCatalogQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/skills")
@Validated
@Tag(name = "Навыки", description = "Справочник навыков: категории, каталог, CRUD")
public class SkillController {

    private final CreateSkillUseCase createSkillUseCase;
    private final DeleteSkillUseCase deleteSkillUseCase;
    private final GetSkillDetailsQuery getSkillDetailsQuery;
    private final ListSkillsCatalogQuery listSkillsCatalogQuery;
    private final ListSkillCategoriesQuery listSkillCategoriesQuery;

    public SkillController(
            final CreateSkillUseCase createSkillUseCase,
            final DeleteSkillUseCase deleteSkillUseCase,
            final GetSkillDetailsQuery getSkillDetailsQuery,
            final ListSkillsCatalogQuery listSkillsCatalogQuery,
            final ListSkillCategoriesQuery listSkillCategoriesQuery
    ) {
        this.createSkillUseCase = createSkillUseCase;
        this.deleteSkillUseCase = deleteSkillUseCase;
        this.getSkillDetailsQuery = getSkillDetailsQuery;
        this.listSkillsCatalogQuery = listSkillsCatalogQuery;
        this.listSkillCategoriesQuery = listSkillCategoriesQuery;
    }

    @GetMapping("/categories")
    @Operation(summary = "Список категорий навыков")
    public ResponseEntity<List<String>> listCategories() {
        final List<String> categories = listSkillCategoriesQuery.execute();
        return ResponseEntity.ok(categories);
    }

    @GetMapping
    @Operation(summary = "Каталог навыков", description = "Поиск по имени, пагинация")
    public ResponseEntity<PageResult<SkillResponseDto>> list(
            @RequestParam(required = false) final String name,
            @RequestParam(defaultValue = "1") @Min(1) final int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(500) final int size
    ) {
        final PageQuery pageQuery = new PageQuery(page, size);
        final PageResult<SkillResponseDto> result = listSkillsCatalogQuery.execute(name, pageQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{skillId}")
    @Operation(summary = "Карточка навыка")
    public ResponseEntity<SkillResponseDto> getById(@PathVariable final Integer skillId) {
        final Optional<SkillResponseDto> dto = getSkillDetailsQuery.execute(skillId);
        return dto.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык не найден"));
    }

    @PostMapping
    @Operation(summary = "Создать навык")
    public ResponseEntity<Integer> create(@Valid @RequestBody final SkillCreateRequest request) {
        final Integer skillId = createSkillUseCase.execute(request.getName(), request.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(skillId);
    }

    @DeleteMapping("/{skillId}")
    @Operation(summary = "Удалить навык")
    public ResponseEntity<Void> delete(@PathVariable final Integer skillId) {
        if (!deleteSkillUseCase.execute(skillId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык не найден");
        }
        return ResponseEntity.noContent().build();
    }
}
