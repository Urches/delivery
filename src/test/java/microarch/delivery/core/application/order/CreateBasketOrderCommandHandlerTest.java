package microarch.delivery.core.application.order;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.command.order.CreateBasketOrderCommand;
import microarch.delivery.core.application.command.order.CreateBasketOrderCommandHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClientPort;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для CreateBasketOrderCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CreateBasketOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GeoClientPort geoClientPort;

    private CreateBasketOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateBasketOrderCommandHandler(orderRepository, geoClientPort);
    }

    @Test
    void shouldCreateOrderWithMultipleItems() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        var items = List.of(new CreateBasketOrderCommand.BasketItem("item-1", "good-1", "Product 1", 100.0, 2),
                new CreateBasketOrderCommand.BasketItem("item-2", "good-2", "Product 2", 200.0, 1));

        var command = CreateBasketOrderCommand.create(basketId, "Russia", "Moscow", "Lenina", "25", "30", 15, items);

        when(geoClientPort.getGeolocationByStreet("Lenina")).thenReturn(Result.success(Location.mustCreate(3, 4)));

        // Act
        Result<Order, Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var order = result.getValue();
        assertThat(order.getId()).isEqualTo(basketId);
        assertThat(command.getItems()).hasSize(2);
        assertThat(command.getItems().get(0).title()).isEqualTo("Product 1");
        assertThat(command.getItems().get(1).title()).isEqualTo("Product 2");

        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnErrorWhenGeoServiceFails() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        var command = CreateBasketOrderCommand.create(basketId, "Russia", "Moscow", "Unknown Street", "1", "1", 5,
                List.of());

        var expectedError = Error.of("geo.error", "Street not found");
        when(geoClientPort.getGeolocationByStreet("Unknown Street")).thenReturn(Result.failure(expectedError));

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(expectedError);
        verify(orderRepository, never()).save(any());
    }
}
