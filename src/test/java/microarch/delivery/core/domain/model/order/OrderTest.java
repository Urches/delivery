package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.events.OrderAssignedEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Aggregate")
class OrderTest {

    @Nested
    @DisplayName("Создание Order")
    class CreationTests {

        @Test
        @DisplayName("Должен успешно создаваться с валидными параметрами")
        void shouldCreateOrderWithValidParameters() {
            var id = UUID.randomUUID();
            var locationResult = Location.create(5, 5);
            var volumeResult = Volume.create(10);

            var orderResult = Order.create(id, locationResult.getValue(), volumeResult.getValue());

            assertTrue(orderResult.isSuccess());
            var order = orderResult.getValue();
            assertNotNull(order);
            assertEquals(id, order.getId());
            assertEquals(locationResult.getValue(), order.getLocation());
            assertEquals(volumeResult.getValue(), order.getVolume());
            assertEquals(OrderStatus.CREATED, order.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Id")
        void shouldReturnErrorWhenIdIsNull() {
            var locationResult = Location.create(5, 5);
            var volumeResult = Volume.create(10);

            var orderResult = Order.create(null, locationResult.getValue(), volumeResult.getValue());

            assertTrue(orderResult.isFailure());
            assertNotNull(orderResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Location")
        void shouldReturnErrorWhenLocationIsNull() {
            var id = UUID.randomUUID();
            var volumeResult = Volume.create(10);

            var orderResult = Order.create(id, null, volumeResult.getValue());

            assertTrue(orderResult.isFailure());
            assertNotNull(orderResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Volume")
        void shouldReturnErrorWhenVolumeIsNull() {
            var id = UUID.randomUUID();
            var locationResult = Location.create(5, 5);

            var orderResult = Order.create(id, locationResult.getValue(), null);

            assertTrue(orderResult.isFailure());
            assertNotNull(orderResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при всех невалидных параметрах")
        void shouldReturnErrorWhenAllParametersAreInvalid() {
            var orderResult = Order.create(null, null, null);

            assertTrue(orderResult.isFailure());
            assertNotNull(orderResult.getError());
        }
    }

    @Nested
    @DisplayName("Смена статуса на ASSIGNED")
    class AssignTests {

        @Test
        @DisplayName("Должен успешно менять статус с CREATED на ASSIGNED")
        void shouldAssignOrderFromCreatedStatus() {
            var order = createValidOrder();

            var result = order.assign();

            assertTrue(result.isSuccess());
            assertEquals(OrderStatus.ASSIGNED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке назначить заказ со статусом ASSIGNED")
        void shouldReturnErrorWhenAssigningAlreadyAssignedOrder() {
            var order = createValidOrder();
            order.assign();

            var result = order.assign();

            assertTrue(result.isFailure());
            assertNotNull(result.getError());
            assertEquals(OrderStatus.ASSIGNED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке назначить заказ со статусом COMPLETED")
        void shouldReturnErrorWhenAssigningCompletedOrder() {
            var order = createValidOrder();
            order.assign();
            order.complete();

            var result = order.assign();

            assertTrue(result.isFailure());
            assertNotNull(result.getError());
            assertEquals(OrderStatus.COMPLETED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Смена статуса на COMPLETED")
    class CompleteTests {

        @Test
        @DisplayName("Должен успешно менять статус с ASSIGNED на COMPLETED")
        void shouldCompleteOrderFromAssignedStatus() {
            var order = createValidOrder();
            order.assign();

            var result = order.complete();

            assertTrue(result.isSuccess());
            assertEquals(OrderStatus.COMPLETED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке завершить заказ со статусом CREATED")
        void shouldReturnErrorWhenCompletingCreatedOrder() {
            var order = createValidOrder();

            var result = order.complete();

            assertTrue(result.isFailure());
            assertNotNull(result.getError());
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertDomainEventForStatues(order);
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке завершить уже завершенный заказ")
        void shouldReturnErrorWhenCompletingAlreadyCompletedOrder() {
            var order = createValidOrder();
            order.assign();
            order.complete();

            var result = order.complete();

            assertTrue(result.isFailure());
            assertNotNull(result.getError());
            assertEquals(OrderStatus.COMPLETED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Полный жизненный цикл заказа")
    class LifecycleTests {

        @Test
        @DisplayName("Должен проходить полный цикл: CREATED -> ASSIGNED -> COMPLETED")
        void shouldCompleteFullLifecycle() {
            var order = createValidOrder();

            assertEquals(OrderStatus.CREATED, order.getStatus());

            var assignResult = order.assign();
            assertTrue(assignResult.isSuccess());
            assertEquals(OrderStatus.ASSIGNED, order.getStatus());

            var completeResult = order.complete();
            assertTrue(completeResult.isSuccess());
            assertEquals(OrderStatus.COMPLETED, order.getStatus());
            assertDomainEventForStatues(order, OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("Не должен пропускать статус ASSIGNED")
        void shouldNotSkipAssignedStatus() {
            var order = createValidOrder();

            var completeResult = order.complete();

            assertTrue(completeResult.isFailure());
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertTrue(order.getDomainEvents().isEmpty());
            assertDomainEventForStatues(order);
        }
    }

    private Order createValidOrder() {
        var id = UUID.randomUUID();
        var location = Location.mustCreate(5, 5);
        var volume = Volume.mustCreate(10);
        return Order.mustCreate(id, location, volume);
    }

    private static void assertDomainEventForStatues(Order order, OrderStatus... statuses) {
        var domainEvents = order.getDomainEvents();
        assertEquals(statuses.length, domainEvents.size(), "Domain events size mismatch");

        for (int i = 0; i < statuses.length; i++) {
            var event = domainEvents.get(i);
            switch (statuses[i]) {
                case ASSIGNED ->
                        assertEquals(order.getId(), ((OrderAssignedEvent) event).getOrderId());
                case COMPLETED ->
                        assertEquals(order.getId(), ((OrderCompletedEvent) event).getOrderId());
                case CREATED ->
                    throw new IllegalArgumentException("Status CREATED has no domain event");
            }
        }
    }
}
