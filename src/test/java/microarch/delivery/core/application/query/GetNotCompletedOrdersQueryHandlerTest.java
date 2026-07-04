package microarch.delivery.core.application.query;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для GetNotCompletedOrdersQueryHandler.
 */
@ExtendWith(MockitoExtension.class)
class GetNotCompletedOrdersQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    private GetNotCompletedOrdersQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetNotCompletedOrdersQueryHandler(orderRepository);
    }

    @Test
    void shouldReturnAllNotCompletedOrdersSuccessfully() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        Order order1 = Order.mustCreate(orderId1, Location.mustCreate(1, 1), Volume.mustCreate(5));
        Order order2 = Order.mustCreate(orderId2, Location.mustCreate(2, 2), Volume.mustCreate(10));

        when(orderRepository.getAllNotCompleted()).thenReturn(List.of(order1, order2));

        GetNotCompletedOrdersQuery query = GetNotCompletedOrdersQuery.create().getValueOrThrow();

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValueOrThrow()).hasSize(2);
        verify(orderRepository, times(1)).getAllNotCompleted();
    }

    @Test
    void shouldReturnEmptyListWhenNoNotCompletedOrders() {
        // Arrange
        when(orderRepository.getAllNotCompleted()).thenReturn(List.of());

        GetNotCompletedOrdersQuery query = GetNotCompletedOrdersQuery.create().getValueOrThrow();

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValueOrThrow()).isEmpty();
    }
}
