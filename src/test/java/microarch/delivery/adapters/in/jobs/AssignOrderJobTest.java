package microarch.delivery.adapters.in.jobs;

import libs.errs.Result;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.application.command.AssignOrderCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для AssignOrderJob.
 */
@ExtendWith(MockitoExtension.class)
class AssignOrderJobTest {

    @Mock
    private AssignOrderCommandHandler assignOrderCommandHandler;

    private AssignOrderJob job;

    @BeforeEach
    void setUp() {
        job = new AssignOrderJob(assignOrderCommandHandler);
    }

    @Test
    void shouldExecuteJobSuccessfully() {
        // Arrange
        var context = mock(JobExecutionContext.class);
        when(assignOrderCommandHandler.handle(any(AssignOrderCommand.class))).thenReturn(Result.success());

        // Act
        job.execute(context);

        // Assert
        verify(assignOrderCommandHandler).handle(any(AssignOrderCommand.class));
    }
}
