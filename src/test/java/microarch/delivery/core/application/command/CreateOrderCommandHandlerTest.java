package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.GeoClientPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для CreateOrderCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GeoClientPort geoClientPort;

    private CreateOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderCommandHandler(orderRepository, geoClientPort);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, "Russia", "Moscow", "Tverskaya", "10", "5", 5)
                .getValueOrThrow();

        // Mock GeoClientPort to return a valid location
        when(geoClientPort.getGeolocationByStreet(anyString())).thenReturn(Result.success(Location.mustCreate(5, 5)));

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
        assertThat(savedOrder.getLocation().getX()).isEqualTo(5);
        assertThat(savedOrder.getLocation().getY()).isEqualTo(5);
    }

    @Test
    void shouldCreateOrderWithDifferentVolumes() {
        // Arrange
        var command1 = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street1", "1", "1", 1).getValueOrThrow();
        var command2 = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street2", "2", "2", 15).getValueOrThrow();

        // Mock GeoClientPort to return valid locations
        when(geoClientPort.getGeolocationByStreet("Street1")).thenReturn(Result.success(Location.mustCreate(1, 1)));
        when(geoClientPort.getGeolocationByStreet("Street2")).thenReturn(Result.success(Location.mustCreate(2, 2)));

        // Act
        var result1 = handler.handle(command1);
        var result2 = handler.handle(command2);

        // Assert
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();

        verify(orderRepository, times(2)).save(any());
    }

    @Test
    void shouldFailWhenGeoServiceReturnsError() {
        // Arrange
        CreateOrderCommand command = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street1", "1", "1", 1).getValueOrThrow();

        // Mock GeoClientPort to return an error
        when(geoClientPort.getGeolocationByStreet(anyString()))
                .thenReturn(Result.failure(Error.of("geo.service.error", "Failed to get geolocation")));

        // Act
        Result<Order, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isNotNull();
        verify(orderRepository, times(0)).save(any());
    }

    @Test
    void shouldCallGeoServiceWithCorrectStreet() {
        // Arrange
        String expectedStreet = "Lenina";
        CreateOrderCommand command = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", expectedStreet, "1", "1", 1).getValueOrThrow();
        when(geoClientPort.getGeolocationByStreet(expectedStreet))
                .thenReturn(Result.success(Location.mustCreate(3, 4)));

        // Act
        handler.handle(command);

        // Assert
        verify(geoClientPort, times(1)).getGeolocationByStreet(expectedStreet);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void shouldNotSaveOrderWhenGeoServiceFails() {
        // Arrange
        CreateOrderCommand command = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street", "1", "1", 1).getValueOrThrow();
        when(geoClientPort.getGeolocationByStreet(anyString()))
                .thenReturn(Result.failure(Error.of("geo.error", "Street not found")));

        // Act
        Result<Order, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isFailure()).isTrue();
        verify(orderRepository, times(0)).save(any());
        verify(geoClientPort, times(1)).getGeolocationByStreet(anyString());
    }

    @Test
    void shouldUseLocationFromGeoService() {
        // Arrange
        int expectedX = 7;
        int expectedY = 8;
        CreateOrderCommand command = CreateOrderCommand
                .create(UUID.randomUUID(), "Russia", "Moscow", "Street", "1", "1", 1).getValueOrThrow();
        when(geoClientPort.getGeolocationByStreet(anyString()))
                .thenReturn(Result.success(Location.mustCreate(expectedX, expectedY)));

        // Act
        Result<Order, ?> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        Order order = result.getValue();
        assertThat(order.getLocation().getX()).isEqualTo(expectedX);
        assertThat(order.getLocation().getY()).isEqualTo(expectedY);
    }
}
