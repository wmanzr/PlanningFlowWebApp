package RUT.PlanningFlow.application.service.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EventPostMortemAiAsyncRunner {

    private final EventPostMortemAiProcessor processor;

    public EventPostMortemAiAsyncRunner(final EventPostMortemAiProcessor processor) {
        this.processor = processor;
    }

    @Async("planningPostMortemExecutor")
    public void runCompletionAsync(final Integer eventId) {
        processor.completeReportFromSnapshot(eventId);
    }
}
