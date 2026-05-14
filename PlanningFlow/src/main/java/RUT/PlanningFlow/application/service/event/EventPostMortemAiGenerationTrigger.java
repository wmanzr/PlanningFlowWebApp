package RUT.PlanningFlow.application.service.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EventPostMortemAiGenerationTrigger {

    private final EventPostMortemAiAsyncRunner asyncRunner;

    public EventPostMortemAiGenerationTrigger(final EventPostMortemAiAsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
    }

    
    public void scheduleAfterCommit(final Integer eventId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            asyncRunner.runCompletionAsync(eventId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncRunner.runCompletionAsync(eventId);
            }
        });
    }
}
