package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import libs.errs.Result;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;
import java.util.UUID;

/**
 * Unit тесты для CreateCourierCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CreateCourierCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private Random random;

    private CreateCourierCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(random.nextInt(10)).thenReturn(5);
        handler = new CreateCourierCommandHandler(courierRepository, random);
    }

    @Test
    void shouldCreateCourierSuccessfully() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        String courierName = "Ivan";
        CreateCourierCommand command = CreateCourierCommand.create(courierId, courierName).getValueOrThrow();

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
        assertThat(savedCourier.getLocation().getX()).isEqualTo(6);
        assertThat(savedCourier.getLocation().getY()).isEqualTo(6);
        assertThat(savedCourier.getMaxVolume().getValue()).isEqualTo(20);
    }

    @Test
    void shouldCreateMultipleCouriers() {
        // Arrange
        CreateCourierCommand command1 = CreateCourierCommand.create(UUID.randomUUID(), "Courier1").getValueOrThrow();
        CreateCourierCommand command2 = CreateCourierCommand.create(UUID.randomUUID(), "Courier2").getValueOrThrow();
        CreateCourierCommand command3 = CreateCourierCommand.create(UUID.randomUUID(), "Courier3").getValueOrThrow();

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
