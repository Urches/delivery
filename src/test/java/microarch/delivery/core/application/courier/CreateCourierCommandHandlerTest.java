package microarch.delivery.core.application.courier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import libs.errs.Result;
import microarch.delivery.core.application.command.courier.CreateCourierCommand;
import microarch.delivery.core.application.command.courier.CreateCourierCommandHandler;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

/**
 * Unit тесты для CreateCourierCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CreateCourierCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    private CreateCourierCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateCourierCommandHandler(courierRepository);
    }

    @Test
    void shouldCreateCourierSuccessfully() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        String courierName = "Ivan";
        CreateCourierCommand command = new CreateCourierCommand(courierId, courierName);

        // Act
        Result<Void, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        ArgumentCaptor<microarch.delivery.core.domain.model.courier.Courier> courierCaptor = ArgumentCaptor
                .forClass(microarch.delivery.core.domain.model.courier.Courier.class);
        verify(courierRepository, times(1)).save(courierCaptor.capture());

        microarch.delivery.core.domain.model.courier.Courier savedCourier = courierCaptor.getValue();
        assertThat(savedCourier.getId()).isEqualTo(courierId);
        assertThat(savedCourier.getName()).isEqualTo(courierName);
        assertThat(savedCourier.getLocation()).isNotNull();
        assertThat(savedCourier.getLocation().getX()).isBetween(1, 10);
        assertThat(savedCourier.getLocation().getY()).isBetween(1, 10);
        assertThat(savedCourier.getMaxVolume().getValue()).isEqualTo(20);
    }

    @Test
    void shouldCreateMultipleCouriers() {
        // Arrange
        CreateCourierCommand command1 = new CreateCourierCommand(UUID.randomUUID(), "Courier1");
        CreateCourierCommand command2 = new CreateCourierCommand(UUID.randomUUID(), "Courier2");
        CreateCourierCommand command3 = new CreateCourierCommand(UUID.randomUUID(), "Courier3");

        // Act
        Result<Void, ?> result1 = handler.handle(command1);
        Result<Void, ?> result2 = handler.handle(command2);
        Result<Void, ?> result3 = handler.handle(command3);

        // Assert
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();
        assertThat(result3.isSuccess()).isTrue();

        verify(courierRepository, times(3)).save(any());
    }
}
