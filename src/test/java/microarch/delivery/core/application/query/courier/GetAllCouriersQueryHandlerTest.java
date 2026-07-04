package microarch.delivery.core.application.query.courier;

import microarch.delivery.core.application.query.GetAllCouriersQuery;
import microarch.delivery.core.application.query.GetAllCouriersQueryHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
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
 * Unit тесты для GetAllCouriersQueryHandler.
 */
@ExtendWith(MockitoExtension.class)
class GetAllCouriersQueryHandlerTest {

    @Mock
    private CourierRepository courierRepository;

    private GetAllCouriersQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetAllCouriersQueryHandler(courierRepository);
    }

    @Test
    void shouldReturnAllCouriersSuccessfully() {
        // Arrange
        UUID courierId1 = UUID.randomUUID();
        UUID courierId2 = UUID.randomUUID();

        Courier courier1 = Courier.mustCreate(courierId1, "Courier 1", Location.mustCreate(1, 1));
        Courier courier2 = Courier.mustCreate(courierId2, "Courier 2", Location.mustCreate(2, 2));

        when(courierRepository.getAll()).thenReturn(List.of(courier1, courier2));

        var query = GetAllCouriersQuery.create().getValueOrThrow();

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValueOrThrow()).hasSize(2);
        verify(courierRepository, times(1)).getAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCouriers() {
        // Arrange
        when(courierRepository.getAll()).thenReturn(List.of());

        var query = GetAllCouriersQuery.create().getValueOrThrow();

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValueOrThrow()).isEmpty();
    }
}
