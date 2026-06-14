package microarch.delivery.adapters.out.postgres.order;

import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Должен добавлять заказ в базу данных")
    void shouldSaveOrder() {
        var location = Location.mustCreate(5, 5);
        var volume = Volume.mustCreate(10);
        var order = Order.of(UUID.randomUUID(), location, volume, OrderStatus.CREATED);

        orderRepository.save(order);

        var saved = orderRepository.getById(order.getId());

        assertThat(saved).isPresent();
    }

    @Test
    @DisplayName("Должен обновлять существующий заказ")
    void shouldUpdateOrder() {
        var location = Location.mustCreate(3, 3);
        var volume = Volume.mustCreate(15);
        var order = Order.of(UUID.randomUUID(), location, volume, OrderStatus.CREATED);

        orderRepository.save(order);
        order.assign();
        orderRepository.update(order);

        var updated = orderRepository.getById(order.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Должен получать заказ по идентификатору")
    void shouldGetOrderById() {
        var location = Location.mustCreate(7, 7);
        var volume = Volume.mustCreate(20);
        var id = UUID.randomUUID();
        var order = Order.of(id, location, volume, OrderStatus.CREATED);

        orderRepository.save(order);
        var getResult = orderRepository.getById(id);

        assertThat(getResult).isPresent();
        assertThat(getResult.get().getId()).isEqualTo(id);
        assertThat(getResult.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional для несуществующего заказа")
    void shouldReturnEmptyForNonExistentOrder() {
        var result = orderRepository.getById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен получать один новый заказ (CREATED)")
    void shouldGetOneNewOrder() {
        var location = Location.mustCreate(2, 2);
        var volume = Volume.mustCreate(5);
        var order = Order.of(UUID.randomUUID(), location, volume, OrderStatus.CREATED);

        orderRepository.save(order);
        var result = orderRepository.getOneNew();

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional если нет новых заказов")
    void shouldReturnEmptyWhenNoNewOrders() {
        var result = orderRepository.getOneNew();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен получать все назначенные заказы (ASSIGNED)")
    void shouldGetAllAssignedOrders() {
        var location1 = Location.mustCreate(1, 1);
        var volume1 = Volume.mustCreate(8);
        var order1 = Order.of(UUID.randomUUID(), location1, volume1, OrderStatus.CREATED);
        order1.assign();

        var location2 = Location.mustCreate(4, 4);
        var volume2 = Volume.mustCreate(12);
        var order2 = Order.of(UUID.randomUUID(), location2, volume2, OrderStatus.CREATED);
        order2.assign();

        orderRepository.save(order1);
        orderRepository.save(order2);

        var result = orderRepository.getAllAssigned();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o -> o.getStatus() == OrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Должен возвращать пустой список если нет назначенных заказов")
    void shouldReturnEmptyListWhenNoAssignedOrders() {
        var result = orderRepository.getAllAssigned();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен получать заказ после обновления статуса на ASSIGNED")
    void shouldGetOrderAfterStatusChangeToAssigned() {
        var location = Location.mustCreate(6, 6);
        var volume = Volume.mustCreate(25);
        var id = UUID.randomUUID();
        var order = Order.of(id, location, volume, OrderStatus.CREATED);

        orderRepository.save(order);

        var getResultBefore = orderRepository.getById(id);
        assertThat(getResultBefore).isPresent();
        assertThat(getResultBefore.get().getStatus()).isEqualTo(OrderStatus.CREATED);

        order.assign();
        orderRepository.update(order);

        var getResultAfter = orderRepository.getById(id);
        assertThat(getResultAfter).isPresent();
        assertThat(getResultAfter.get().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Должен получать заказ после обновления статуса на COMPLETED")
    void shouldGetOrderAfterStatusChangeToCompleted() {
        var location = Location.mustCreate(8, 8);
        var volume = Volume.mustCreate(30);
        var id = UUID.randomUUID();
        var order = Order.of(id, location, volume, OrderStatus.CREATED);

        orderRepository.save(order);
        order.assign();
        order.complete();
        orderRepository.update(order);

        var getResult = orderRepository.getById(id);
        assertThat(getResult).isPresent();
        assertThat(getResult.get().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Должен получать все назначенные заказы после множественного добавления")
    void shouldGetAllAssignedOrdersAfterMultipleAdds() {
        for (int i = 1; i <= 5; i++) {
            var location = Location.mustCreate(i, i);
            var volume = Volume.mustCreate(i + 1);
            var order = Order.of(UUID.randomUUID(), location, volume, OrderStatus.CREATED);
            order.assign();
            orderRepository.save(order);
        }

        var result = orderRepository.getAllAssigned();

        assertThat(result).hasSize(5);
    }
}
