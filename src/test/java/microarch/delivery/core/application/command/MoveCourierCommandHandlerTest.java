package microarch.delivery.core.application.command;

import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для MoveCourierCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class MoveCourierCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    private MoveCourierCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MoveCourierCommandHandler(courierRepository);
    }

    @Test
    void shouldMoveCourierSuccessfully() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        int newX = 5;
        int newY = 7;
        var newLocation = Location.mustCreate(newX, newY);
        var command = MoveCourierCommand.create(courierId, newLocation).getValueOrThrow();

        Courier courier = Courier.mustCreate(courierId, "Test Courier", Location.mustCreate(1, 1));
        when(courierRepository.getById(courierId)).thenReturn(Optional.of(courier));

        // Act
        Result<Void, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        ArgumentCaptor<Courier> courierCaptor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository, times(1)).update(courierCaptor.capture());

        Courier updatedCourier = courierCaptor.getValue();
        assertThat(updatedCourier.getLocation().getX()).isEqualTo(newX);
        assertThat(updatedCourier.getLocation().getY()).isEqualTo(newY);
    }

    @Test
    void shouldThrowExceptionWhenCourierNotFound() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        MoveCourierCommand command = MoveCourierCommand.create(courierId, Location.mustCreate(5, 7)).getValueOrThrow();

        when(courierRepository.getById(courierId)).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
