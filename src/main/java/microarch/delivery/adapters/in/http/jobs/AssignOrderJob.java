package microarch.delivery.adapters.in.http.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.command.assignment.AssignOrderCommand;
import microarch.delivery.core.application.command.assignment.AssignOrderCommandHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Quartz Job для назначения заказов на курьеров.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignOrderJob implements Job {

    private final AssignOrderCommandHandler assignOrderCommandHandler;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            var result = AssignOrderCommand.create().flatMap(assignOrderCommandHandler::handle);
            if (result.isFailure()) {
                // Логгируем ошибку, но не прерываем выполнение
                log.error("Failed to assign order: {}", result.getError().getMessage());
            }
        } catch (Exception e) {
            // Логгируем исключение, но не прерываем выполнение
            log.error("Error during order assignment: {}", e.getMessage());
        }
    }
}
