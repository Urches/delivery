package microarch.delivery.adapters.in.http.jobs;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.command.assignment.AssignOrderCommand;
import microarch.delivery.core.application.command.assignment.AssignOrderCommandHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Регулярная задача для назначения заказов на курьеров. Запускается каждую 1 секунду.
 */
@Component
@RequiredArgsConstructor
public class AssignOrderJob {

    private final AssignOrderCommandHandler assignOrderCommandHandler;

    @Scheduled(fixedDelay = 1000) // Запускается каждую 1 секунду
    public void run() {
        try {
            var result = AssignOrderCommand.create().flatMap(assignOrderCommandHandler::handle);
            if (result.isFailure()) {
                // Логгируем ошибку, но не прерываем выполнение
                System.err.println("Failed to assign order: " + result.getError().getMessage());
            }
        } catch (Exception e) {
            // Логгируем исключение, но не прерываем выполнение
            System.err.println("Error during order assignment: " + e.getMessage());
        }
    }
}
