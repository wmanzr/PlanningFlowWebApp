package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.EventAiPostmortemReportEntity;

import java.util.Optional;

public interface EventAiPostmortemReportRepository extends BaseRepository<EventAiPostmortemReportEntity, Integer> {
    Optional<EventAiPostmortemReportEntity> findByEvent_Id(Integer eventId);
}
