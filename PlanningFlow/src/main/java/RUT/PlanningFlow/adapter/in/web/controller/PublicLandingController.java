package RUT.PlanningFlow.adapter.in.web.controller;

import RUT.PlanningFlow.application.dto.landing.PublicLandingStatsDto;
import RUT.PlanningFlow.application.service.landing.GetPublicLandingStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Публичные данные", description = "Без авторизации")
public class PublicLandingController {

    private final GetPublicLandingStatsService getPublicLandingStatsService;

    public PublicLandingController(final GetPublicLandingStatsService getPublicLandingStatsService) {
        this.getPublicLandingStatsService = getPublicLandingStatsService;
    }

    @GetMapping("/landing-stats")
    @Operation(summary = "Статистика для лендинга", description = "Число завершённых мероприятий в системе", security = {})
    public PublicLandingStatsDto landingStats() {
        return getPublicLandingStatsService.getLandingStats();
    }
}
