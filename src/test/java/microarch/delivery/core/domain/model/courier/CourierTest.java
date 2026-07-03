package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Courier Aggregate")
class CourierTest {

    @Nested
    @DisplayName("Создание Courier")
    class CreationTests {

        @Test
        @DisplayName("Должен успешно создаваться с валидными параметрами")
        void shouldCreateCourierWithValidParameters() {
            var id = UUID.randomUUID();
            var name = "John Doe";
            var locationResult = Location.create(5, 5);

            var courierResult = Courier.create(id, name, locationResult.getValue());

            assertTrue(courierResult.isSuccess());
            var courier = courierResult.getValue();
            assertNotNull(courier);
            assertEquals(id, courier.getId());
            assertEquals(name, courier.getName());
            assertEquals(locationResult.getValue(), courier.getLocation());
            assertEquals(20, courier.getMaxVolume().getValue());
            assertTrue(courier.getAssignments().isEmpty());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Id")
        void shouldReturnErrorWhenIdIsNull() {
            var name = "John Doe";
            var locationResult = Location.create(5, 5);

            var courierResult = Courier.create(null, name, locationResult.getValue());

            assertTrue(courierResult.isFailure());
            assertNotNull(courierResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при пустом имени")
        void shouldReturnErrorWhenNameIsEmpty() {
            var id = UUID.randomUUID();
            var locationResult = Location.create(5, 5);

            var courierResult = Courier.create(id, "", locationResult.getValue());

            assertTrue(courierResult.isFailure());
            assertNotNull(courierResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null имени")
        void shouldReturnErrorWhenNameIsNull() {
            var id = UUID.randomUUID();
            var locationResult = Location.create(5, 5);

            var courierResult = Courier.create(id, null, locationResult.getValue());

            assertTrue(courierResult.isFailure());
            assertNotNull(courierResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Location")
        void shouldReturnErrorWhenLocationIsNull() {
            var id = UUID.randomUUID();
            var name = "John Doe";

            var courierResult = Courier.create(id, name, null);

            assertTrue(courierResult.isFailure());
            assertNotNull(courierResult.getError());
        }
    }

    @Nested
    @DisplayName("Проверка возможности взять заказ")
    class CanTakeOrderTests {

        @Test
        @DisplayName("Должен возвращать true для заказа, который не превышает максимум")
        void shouldReturnTrueWhenOrderFitsMaxVolume() {
            var courier = createValidCourier();
            var order = createOrderWithVolume(15);

            assertTrue(courier.canTakeOrder(order).getValue());
        }

        @Test
        @DisplayName("Должен возвращать true для заказа, который ровно равен максимуму")
        void shouldReturnTrueWhenOrderEqualsMaxVolume() {
            var courier = createValidCourier();
            var order = createOrderWithVolume(20);

            assertTrue(courier.canTakeOrder(order).getValue());
        }

        @Test
        @DisplayName("Должен возвращать false для заказа, который превышает максимум")
        void shouldReturnFalseWhenOrderExceedsMaxVolume() {
            var courier = createValidCourier();
            var order = createOrderWithVolume(25);

            assertFalse(courier.canTakeOrder(order).getValue());
        }
    }

    @Nested
    @DisplayName("Взять заказ")
    class TakeOrderTests {

        @Test
        @DisplayName("Должен успешно брать заказ в работу")
        void shouldTakeOrderSuccessfully() {
            var courier = createValidCourier();
            var order = createValidOrder();

            var result = courier.takeOrder(order);

            assertTrue(result.isSuccess());
            assertEquals(1, courier.getAssignments().size());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке взять заказ, превышающий максимум")
        void shouldReturnErrorWhenOrderExceedsMaxVolume() {
            var courier = createValidCourier();
            var order = createOrderWithVolume(25);

            var result = courier.takeOrder(order);

            assertTrue(result.isFailure());
            assertNotNull(result.getError());
            assertTrue(courier.getAssignments().isEmpty());
        }

        @Test
        @DisplayName("Должен позволять взять несколько заказов в пределах максимума")
        void shouldAllowMultipleOrdersWithinMaxVolume() {
            var courier = createValidCourier();
            var order1 = createOrderWithVolume(5);
            var order2 = createOrderWithVolume(10);
            var order3 = createOrderWithVolume(5);

            assertTrue(courier.takeOrder(order1).isSuccess());
            assertTrue(courier.takeOrder(order2).isSuccess());
            assertTrue(courier.takeOrder(order3).isSuccess());

            assertEquals(3, courier.getAssignments().size());
            assertEquals(20, courier.getCurrentVolume().orElseThrow().getValue());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при превышении максимума после нескольких заказов")
        void shouldReturnErrorWhenAddingOrderExceedsMaxAfterMultipleOrders() {
            var courier = createValidCourier();
            var order1 = createOrderWithVolume(10);
            var order2 = createOrderWithVolume(15);

            assertTrue(courier.takeOrder(order1).isSuccess());
            assertTrue(courier.takeOrder(order2).isFailure());
            assertEquals(1, courier.getAssignments().size());
        }
    }

    @Nested
    @DisplayName("Завершение Assignment")
    class CompleteAssignmentTests {

        @Test
        @DisplayName("Должен завершать Assignment только если курьер рядом (расстояние <= 1)")
        void shouldCompleteAssignmentOnlyWhenCourierIsNear() {
            var courierLocation = Location.create(1, 1);
            var courier = createValidCourierWithLocation(courierLocation.getValue());
            var orderLocation = Location.create(3, 4); // расстояние = 5
            var order = createOrderWithLocation(orderLocation.getValue());
            courier.takeOrder(order);

            var result = courier.completeAssignment();

            assertFalse(result);
            assertEquals(1, courier.getAssignments().size());
        }

        @Test
        @DisplayName("Должен завершать Assignment если курьер в той же клетке (расстояние = 0)")
        void shouldCompleteAssignmentWhenCourierInSameLocation() {
            var locationResult = Location.create(5, 5);
            var location = locationResult.getValue();
            var courier = createValidCourierWithLocation(location);
            var order = createOrderWithLocation(location);
            courier.takeOrder(order);

            var result = courier.completeAssignment();

            assertTrue(result);
            assertTrue(courier.getAssignments().isEmpty());
        }

        @Test
        @DisplayName("Должен завершать Assignment если курьер в соседней клетке (расстояние = 1)")
        void shouldCompleteAssignmentWhenCourierInAdjacentCell() {
            var courier = createValidCourierWithLocation(Location.create(5, 5).getValue());
            var order = createOrderWithLocation(
                    // расстояние = 1
                    Location.create(5, 6).getValue());

            courier.takeOrder(order);
            var result = courier.completeAssignment();

            assertTrue(result);
            assertTrue(courier.getAssignments().isEmpty());
        }
    }

    @Nested
    @DisplayName("Перемещение курьера")
    class MoveTests {

        @Test
        @DisplayName("Должен успешно перемещаться в новое местоположение")
        void shouldMoveToNewLocationSuccessfully() {
            var courier = createValidCourier();
            var newLocationResult = Location.create(8, 9);

            var result = courier.move(newLocationResult.getValue());
            assertTrue(result.isSuccess());
            assertEquals(newLocationResult.getValue(), courier.getLocation());

            var location1Result = Location.create(10, 10);
            var location1 = location1Result.getValue();
            assertTrue(courier.move(location1).isSuccess());
            assertEquals(location1, courier.getLocation());

            var location2Result = Location.create(5, 5);
            var location2 = location2Result.getValue();
            assertTrue(courier.move(location2).isSuccess());
            assertEquals(location2, courier.getLocation());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null Location")
        void shouldThrowExceptionWhenLocationIsNull() {
            var courier = createValidCourier();

            assertThrows(NullPointerException.class, () -> courier.move(null));
        }
    }

    private Courier createValidCourier() {
        var id = UUID.randomUUID();
        var name = "John Doe";
        var location = Location.mustCreate(5, 5);
        return Courier.mustCreate(id, name, location);
    }

    private Courier createValidCourierWithLocation(Location location) {
        var id = UUID.randomUUID();
        var name = "John Doe";
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
}
