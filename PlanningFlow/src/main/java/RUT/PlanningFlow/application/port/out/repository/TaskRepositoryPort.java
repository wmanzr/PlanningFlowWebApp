package RUT.PlanningFlow.application.port.out.repository;

import RUT.PlanningFlow.application.pagination.PageQuery;
import RUT.PlanningFlow.application.pagination.PageResult;
import RUT.PlanningFlow.domain.enums.AssignStatus;
import RUT.PlanningFlow.domain.enums.TaskStatus;
import RUT.PlanningFlow.domain.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Optional<Task> findById(Integer id);
    List<Task> findAll();
    PageResult<Task> findTasks(PageQuery pageQuery);
    PageResult<Task> findByTitleContainingIgnoreCase(String searchTerm, PageQuery pageQuery);
    List<Task> findCommittedTasksForUserOnDate(Integer userId, LocalDate date);
    PageResult<Task> findTasksForUser(
            Integer userId,
            List<AssignStatus> assignmentStatuses,
            String titleOrNull,
            PageQuery pageQuery
    );
    PageResult<Task> findTasksForUserBetween(
            Integer userId,
            List<AssignStatus> assignmentStatuses,
            LocalDateTime start,
            LocalDateTime end,
            String titleOrNull,
            PageQuery pageQuery
    );
    List<Task> findTasksForEvent(Integer eventId);
    PageResult<Task> findTasksForEvent(Integer eventId, PageQuery pageQuery);

    
    boolean existsByEventIdAndTitleIgnoreCase(Integer eventId, String title);

    
    boolean existsByEventIdAndTitleIgnoreCaseAndIdNot(Integer eventId, String title, Integer excludeTaskId);
    PageResult<Task> findTasksForEventBetween(Integer eventId, LocalDateTime start, LocalDateTime end, PageQuery pageQuery);
    
    long countTasksAuthoredByUser(Integer userId);

    long countByEventIdAndStatus(Integer eventId, TaskStatus status);

    long countByStatus(TaskStatus status);

    
    long countTasksForEvent(Integer eventId);

    double sumCompletedWorkedHoursForUser(Integer userId);

    Optional<Integer> create(Task task);
    Optional<Integer> update(Task task);
}