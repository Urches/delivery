package microarch.delivery.adapters.out.postgres.courier;

import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private CourierRepository courierRepository;

    @Test
    @DisplayName("Должен добавлять курьера в базу данных")
    void shouldAddCourier() {
        var location = Location.mustCreate(5, 5);
        var courier = Courier.of(UUID.randomUUID(), "John Doe", location, Volume.mustCreate(20), Collections.emptyList());

        courierRepository.save(courier);

        var saved = courierRepository.getById(courier.getId());

        assertThat(saved).isPresent();
    }

    @Test
    @DisplayName("Должен обновлять существующего курьера")
    void shouldUpdateCourier() {
        var location = Location.mustCreate(3, 3);
        var courier = Courier.of(UUID.randomUUID(), "Jane Doe", location, Volume.mustCreate(20), Collections.emptyList());

        courierRepository.save(courier);

        var newLocation = Location.mustCreate(7, 7);
        courier.move(newLocation);

        courierRepository.update(courier);

        var updated = courierRepository.getById(courier.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getLocation().getX()).isEqualTo(7);
        assertThat(updated.get().getLocation().getY()).isEqualTo(7);
    }

    @Test
    @DisplayName("Должен получать курьера по идентификатору")
    void shouldGetCourierById() {
        var location = Location.mustCreate(7, 7);
        var id = UUID.randomUUID();
        var courier = Courier.of(id, "Bob Smith", location, Volume.mustCreate(20), Collections.emptyList());

        courierRepository.save(courier);
        var getResult = courierRepository.getById(id);

        assertThat(getResult).isPresent();
        assertThat(getResult.get().getId()).isEqualTo(id);
        assertThat(getResult.get().getName()).isEqualTo("Bob Smith");
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional для несуществующего курьера")
    void shouldReturnEmptyForNonExistentCourier() {
        var result = courierRepository.getById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен получать всех курьеров")
    void shouldGetAllCouriers() {
        var location1 = Location.mustCreate(1, 1);
        var courier1 = Courier.of(UUID.randomUUID(), "Alice", location1, Volume.mustCreate(20), Collections.emptyList());

        var location2 = Location.mustCreate(4, 4);
        var courier2 = Courier.of(UUID.randomUUID(), "Bob", location2, Volume.mustCreate(20), Collections.emptyList());

        courierRepository.save(courier1);
        courierRepository.save(courier2);

        var result = courierRepository.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Должен возвращать пустой список если нет курьеров")
    void shouldReturnEmptyListWhenNoCouriers() {
        var result = courierRepository.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен получать курьера после перемещения")
    void shouldGetCourierAfterMove() {
        var location = Location.mustCreate(6, 6);
        var id = UUID.randomUUID();
        var courier = Courier.of(id, "Charlie", location, Volume.mustCreate(20), Collections.emptyList());

        courierRepository.save(courier);

        var getResultBefore = courierRepository.getById(id);
        assertThat(getResultBefore).isPresent();
        assertThat(getResultBefore.get().getLocation().getX()).isEqualTo(6);
        assertThat(getResultBefore.get().getLocation().getY()).isEqualTo(6);

        var newLocation = Location.mustCreate(9, 9);
        courier.move(newLocation);
        courierRepository.update(courier);

        var getResultAfter = courierRepository.getById(id);
        assertThat(getResultAfter).isPresent();
        assertThat(getResultAfter.get().getLocation().getX()).isEqualTo(9);
        assertThat(getResultAfter.get().getLocation().getY()).isEqualTo(9);
    }

    @Test
    @DisplayName("Должен получать курьера с назначениями")
    void shouldGetCourierWithAssignments() {
        var location = Location.mustCreate(2, 2);
        var id = UUID.randomUUID();
        var courier = Courier.of(id, "David", location, Volume.mustCreate(20), Collections.emptyList());

        var orderLocation = Location.mustCreate(3, 3);
        var orderVolume = Volume.mustCreate(5);
        var order = Order.of(UUID.randomUUID(), orderLocation, orderVolume, OrderStatus.CREATED);

        courier.takeOrder(order);

        courierRepository.save(courier);

        var getResult = courierRepository.getById(id);
        assertThat(getResult).isPresent();
        assertThat(getResult.get().getAssignments()).hasSize(1);
    }

    @Test
    @DisplayName("Должен обновлять курьера с назначениями")
    void shouldUpdateCourierWithAssignments() {
        var location = Location.mustCreate(5, 5);
        var id = UUID.randomUUID();
        var courier = Courier.of(id, "Eve", location, Volume.mustCreate(20), Collections.emptyList());

        var orderLocation = Location.mustCreate(6, 6);
        var orderVolume = Volume.mustCreate(10);
        var order = Order.of(UUID.randomUUID(), orderLocation, orderVolume, OrderStatus.CREATED);

        courier.takeOrder(order);
        courierRepository.save(courier);

        var newLocation = Location.mustCreate(8, 8);
        courier.move(newLocation);
        courierRepository.update(courier);

        var getResult = courierRepository.getById(id);
        assertThat(getResult).isPresent();
        assertThat(getResult.get().getLocation().getX()).isEqualTo(8);
        assertThat(getResult.get().getLocation().getY()).isEqualTo(8);
        assertThat(getResult.get().getAssignments()).hasSize(1);
    }

    @Test
    @DisplayName("Должен получать нескольких курьеров после множественного добавления")
    void shouldGetAllCouriersAfterMultipleAdds() {
        for (int i = 1; i <= 10; i++) {
            var location = Location.mustCreate(i, i);
            var courier = Courier.of(UUID.randomUUID(), "Courier " + i, location, Volume.mustCreate(20), Collections.emptyList());
            courierRepository.save(courier);
        }

        var result = courierRepository.getAll();

        assertThat(result).hasSize(10);
    }
}
