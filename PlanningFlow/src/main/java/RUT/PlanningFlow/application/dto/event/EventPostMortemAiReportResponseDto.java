package RUT.PlanningFlow.application.dto.event;

public record EventPostMortemAiReportResponseDto(
        String status,
        String reportText,
        String errorMessage,
        String updatedAt
) {
}
