package microarch.delivery.core.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import libs.errs.Result;
import microarch.delivery.core.application.command.order.CreateOrderCommand;
import microarch.delivery.core.application.command.order.CreateOrderCommandHandler;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

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
        CreateOrderCommand command = new CreateOrderCommand(orderId, "Russia", "Moscow", "Tverskaya", "10", "5", 5);

        // Act
        Result<Void, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        ArgumentCaptor<microarch.delivery.core.domain.model.order.Order> orderCaptor = ArgumentCaptor
                .forClass(microarch.delivery.core.domain.model.order.Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        microarch.delivery.core.domain.model.order.Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getId()).isEqualTo(orderId);
        assertThat(savedOrder.getVolume().getValue()).isEqualTo(5);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getLocation()).isNotNull();
        assertThat(savedOrder.getLocation().getX()).isBetween(1, 10);
        assertThat(savedOrder.getLocation().getY()).isBetween(1, 10);
    }

    @Test
    void shouldCreateOrderWithDifferentVolumes() {
        // Arrange
        CreateOrderCommand command1 = new CreateOrderCommand(UUID.randomUUID(), "Russia", "Moscow", "Street1", "1", "1",
                1);
        CreateOrderCommand command2 = new CreateOrderCommand(UUID.randomUUID(), "Russia", "Moscow", "Street2", "2", "2",
                15);

        // Act
        Result<Void, ?> result1 = handler.handle(command1);
        Result<Void, ?> result2 = handler.handle(command2);

        // Assert
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();

        verify(orderRepository, times(2)).save(any());
    }
}
