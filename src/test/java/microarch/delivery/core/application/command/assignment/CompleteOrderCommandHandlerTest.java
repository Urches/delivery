package microarch.delivery.core.application.command.assignment;

import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.application.command.CompleteOrderCommandHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для CompleteOrderCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CompleteOrderCommandHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderRepository orderRepository;

    private CompleteOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompleteOrderCommandHandler(courierRepository, orderRepository);
    }

    @Test
    void shouldCompleteOrderSuccessfully() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        var location = Location.mustCreate(1, 1);

        // Создаем заказ со статусом ASSIGNED (используем of для установки статуса)
        var order = Order.of(orderId, location, Volume.mustCreate(5), OrderStatus.ASSIGNED);
        when(orderRepository.getById(orderId)).thenReturn(Optional.of(order));

        // Создаем курьера с assignment (курьер находится в той же локации, что и заказ)
        var courier = Courier.mustCreate(courierId, "Test Courier", location);
        courier.takeOrder(order);
        when(courierRepository.getById(courierId)).thenReturn(Optional.of(courier));

        var command = CompleteOrderCommand.create(courierId, orderId).getValueOrThrow();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository, times(1)).update(courier);
        verify(orderRepository, times(1)).update(order);
    }

    @Test
    void shouldReturnErrorWhenCourierNotFound() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(courierRepository.getById(courierId)).thenReturn(Optional.empty());

        CompleteOrderCommand command = CompleteOrderCommand.create(courierId, orderId).getValueOrThrow();

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldReturnErrorWhenOrderNotFound() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Courier courier = Courier.mustCreate(courierId, "Test Courier", Location.mustCreate(1, 1));
        when(courierRepository.getById(courierId)).thenReturn(Optional.of(courier));
        when(orderRepository.getById(orderId)).thenReturn(Optional.empty());

        CompleteOrderCommand command = CompleteOrderCommand.create(courierId, orderId).getValueOrThrow();

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
