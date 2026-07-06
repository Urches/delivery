package microarch.delivery.core.application.command;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit тесты для CreateOrderCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    private CreateOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderCommandHandler(orderRepository);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, "Russia", "Moscow", "Tverskaya", "10", "5", 5)
                .getValueOrThrow();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        var savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getId()).isEqualTo(orderId);
        assertThat(savedOrder.getVolume().getValue()).isEqualTo(5);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getLocation()).isNotNull();
    }

    @Test
    void shouldCreateOrderWithDifferentVolumes() {
        // Arrange
        var command1 = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street1", "1", "1", 1).getValueOrThrow();
        var command2 = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street2", "2", "2", 15).getValueOrThrow();

        // Act
        var result1 = handler.handle(command1);
        var result2 = handler.handle(command2);

        // Assert
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();

        verify(orderRepository, times(2)).save(any());
    }
}
