package microarch.delivery.core.application.command.assignment;

import libs.errs.Result;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.application.command.AssignOrderCommandHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для AssignOrderCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class AssignOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderDispatchService dispatchService;

    private AssignOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AssignOrderCommandHandler(orderRepository, courierRepository, dispatchService);
    }

    @Test
    void shouldAssignOrderSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID courierId = UUID.randomUUID();

        var order = Order.mustCreate(orderId, Location.mustCreate(1, 1), Volume.mustCreate(5));
        var courier = Courier.mustCreate(courierId, "Test Courier", Location.mustCreate(1, 1));

        when(orderRepository.getOneNew()).thenReturn(Optional.of(order));
        when(courierRepository.getAll()).thenReturn(List.of(courier));
        when(dispatchService.dispatch(any(Order.class), anyList())).thenReturn(Result.success(courier));

        var command = AssignOrderCommand.create().getValueOrThrow();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository, times(1)).update(order);
        verify(courierRepository, times(1)).update(courier);
    }

    @Test
    void shouldReturnErrorWhenNoNewOrdersAvailable() {
        // Arrange
        when(orderRepository.getOneNew()).thenReturn(Optional.empty());

        var command = AssignOrderCommand.create().getValueOrThrow();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getMessage()).contains("No new orders available");
    }

    @Test
    void shouldReturnErrorWhenDispatchFails() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        var order = Order.mustCreate(orderId, Location.mustCreate(1, 1), Volume.mustCreate(5));

        when(orderRepository.getOneNew()).thenReturn(Optional.of(order));
        when(courierRepository.getAll()).thenReturn(List.of());
        when(dispatchService.dispatch(any(Order.class), any(List.class)))
                .thenReturn(Result.failure(libs.errs.GeneralErrors.invalidOperation("No couriers available")));

        var command = AssignOrderCommand.create().getValueOrThrow();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getMessage()).contains("No couriers available");
    }
}
