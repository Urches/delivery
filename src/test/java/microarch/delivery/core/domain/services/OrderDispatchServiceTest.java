package microarch.delivery.core.domain.services;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OrderDispatchService")
class OrderDispatchServiceTest {

    private final OrderDispatchService dispatchService = new OrderDispatchServiceImpl();

    @Nested
    @DisplayName("Полный цикл диспетчеризации")
    class DispatchTests {

        @Test
        @DisplayName("Должен успешно диспетчеризировать заказ")
        void shouldDispatchOrderSuccessfully() {
            var orderLocation = Location.create(5, 5).getValue();
            var order = createOrderWithLocation(orderLocation);

            var courierLocation = Location.create(6, 6).getValue();
            var courier = createCourierWithLocation(courierLocation);

            var couriers = List.of(courier);

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isSuccess());
            assertEquals(courier.getId(), result.getValue().getId());
            assertEquals(1, courier.getAssignments().size());
            assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку если нет доступных курьеров")
        void shouldReturnErrorWhenNoCouriersAvailable() {
            var order = createValidOrder();
            var couriers = new ArrayList<Courier>();

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isFailure());
        }

        @Test
        @DisplayName("Должен возвращать ошибку если все курьеры переполнены")
        void shouldReturnErrorWhenAllCouriersFull() {
            var order = createOrderWithVolume(5);

            var courier1 = createValidCourier();
            courier1.takeOrder(createOrderWithVolume(20));

            var courier2 = createValidCourier();
            courier2.takeOrder(createOrderWithVolume(18));

            var couriers = List.of(courier1, courier2);

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isFailure());
        }

        @Test
        @DisplayName("Должен выбирать ближайшего курьера и назначать заказ")
        void shouldSelectNearestCourierAndAssign() {
            var orderLocation = Location.create(5, 5).getValue();
            var order = createOrderWithLocation(orderLocation);

            var farCourierLocation = Location.create(1, 1).getValue();
            var farCourier = createCourierWithLocation(farCourierLocation);

            var nearCourierLocation = Location.create(4, 5).getValue();
            var nearCourier = createCourierWithLocation(nearCourierLocation);

            var couriers = List.of(farCourier, nearCourier);

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isSuccess());
            assertEquals(nearCourier.getId(), result.getValue().getId());
            assertEquals(1, nearCourier.getAssignments().size());
            assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку для заказа не в статусе CREATED")
        void shouldReturnErrorForNonCreatedOrder() {
            var order = createOrderWithStatus(OrderStatus.COMPLETED);
            var courier = createValidCourier();
            var couriers = List.of(courier);

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isFailure());
        }

        @Test
        @DisplayName("Должен допускать назначение нескольких заказов одному курьеру")
        void shouldAllowMultipleOrdersToSameCourier() {
            var courierLocation = Location.create(5, 5).getValue();
            var courier = createCourierWithLocation(courierLocation);

            var order1Location = Location.create(5, 6).getValue();
            var order1 = createOrderWithVolumeAndLocation(5, order1Location);

            var order2Location = Location.create(6, 5).getValue();
            var order2 = createOrderWithVolumeAndLocation(5, order2Location);

            var order3Location = Location.create(4, 5).getValue();
            var order3 = createOrderWithVolumeAndLocation(5, order3Location);

            var couriers = List.of(courier);

            var result1 = dispatchService.dispatch(order1, couriers);
            var result2 = dispatchService.dispatch(order2, couriers);
            var result3 = dispatchService.dispatch(order3, couriers);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertTrue(result3.isSuccess());
            assertEquals(3, courier.getAssignments().size());
            assertEquals(OrderStatus.ASSIGNED, order1.getStatus());
            assertEquals(OrderStatus.ASSIGNED, order2.getStatus());
            assertEquals(OrderStatus.ASSIGNED, order3.getStatus());
        }

        @Test
        @DisplayName("Должен фильтровать переполненных курьеров при выборе ближайшего")
        void shouldFilterOutFullCouriersWhenSelectingNearest() {
            var orderLocation = Location.create(5, 5).getValue();
            var order = createOrderWithVolumeAndLocation(10, orderLocation);

            var nearCourierLocation = Location.create(6, 5).getValue();
            var nearCourier = createCourierWithLocation(nearCourierLocation);
            nearCourier.takeOrder(createOrderWithVolume(15));

            var farCourierLocation = Location.create(1, 1).getValue();
            var farCourier = createCourierWithLocation(farCourierLocation);

            var couriers = List.of(nearCourier, farCourier);

            var result = dispatchService.dispatch(order, couriers);

            assertTrue(result.isSuccess());
            assertEquals(farCourier.getId(), result.getValue().getId());
        }
    }

    private Courier createValidCourier() {
        var id = UUID.randomUUID();
        var name = "Test Courier";
        var location = Location.mustCreate(5, 5);
        return Courier.mustCreate(id, name, location);
    }

    private Courier createCourierWithLocation(Location location) {
        var id = UUID.randomUUID();
        var name = "Test Courier";
        return Courier.mustCreate(id, name, location);
    }

    private Order createValidOrder() {
        var id = UUID.randomUUID();
        var volume = Volume.mustCreate(5);
        var location = Location.mustCreate(5, 5);
        return Order.mustCreate(id, location, volume);
    }

    private Order createOrderWithVolume(int volume) {
        var id = UUID.randomUUID();
        return Order.mustCreate(id, Location.mustCreate(5, 5), Volume.mustCreate(volume));
    }

    private Order createOrderWithLocation(Location location) {
        var id = UUID.randomUUID();
        return Order.mustCreate(id, location, Volume.mustCreate(5));
    }

    private Order createOrderWithVolumeAndLocation(int volume, Location location) {
        var id = UUID.randomUUID();
        return Order.mustCreate(id, location, Volume.mustCreate(volume));
    }

    private Order createOrderWithStatus(OrderStatus status) {
        var id = UUID.randomUUID();
        var volume = Volume.mustCreate(5);
        var location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(id, location, volume);
        if (status == OrderStatus.ASSIGNED) {
            order.assign();
        } else if (status == OrderStatus.COMPLETED) {
            order.assign();
            order.complete();
        }
        return order;
    }
}
