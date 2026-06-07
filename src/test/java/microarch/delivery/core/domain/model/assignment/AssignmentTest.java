package microarch.delivery.core.domain.model.assignment;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Volume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Assignment Entity")
class AssignmentTest {

    private static final UUID TEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID TEST_ORDER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    @Nested
    @DisplayName("Создание Assignment")
    class CreationTests {

        @Test
        @DisplayName("Должен успешно создаваться с валидными параметрами и статусом ASSIGNED")
        void shouldCreateAssignmentWithValidParameters() {
            var volumeResult = Volume.create(50);
            var locationResult = Location.create(5, 5);

            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    locationResult.getValue());

            assertTrue(assignmentResult.isSuccess());
            var assignment = assignmentResult.getValue();
            assertNotNull(assignment);
            assertEquals(TEST_ID, assignment.getId());
            assertEquals(TEST_ORDER_ID, assignment.getOrderId());
            assertEquals(50, assignment.getVolume().getValue());
            assertEquals(5, assignment.getLocation().getX());
            assertEquals(5, assignment.getLocation().getY());
            assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null id")
        void shouldReturnErrorWhenIdIsNull() {
            var volumeResult = Volume.create(50);
            var locationResult = Location.create(5, 5);

            var assignmentResult = Assignment.create(null, TEST_ORDER_ID, volumeResult.getValue(),
                    locationResult.getValue());

            assertTrue(assignmentResult.isFailure());
            assertNotNull(assignmentResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null orderId")
        void shouldReturnErrorWhenOrderIdIsNull() {
            var volumeResult = Volume.create(50);
            var locationResult = Location.create(5, 5);

            var assignmentResult = Assignment.create(TEST_ID, null, volumeResult.getValue(), locationResult.getValue());

            assertTrue(assignmentResult.isFailure());
            assertNotNull(assignmentResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null volume")
        void shouldReturnErrorWhenVolumeIsNull() {
            var locationResult = Location.create(5, 5);

            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, null, locationResult.getValue());

            assertTrue(assignmentResult.isFailure());
            assertNotNull(assignmentResult.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при null location")
        void shouldReturnErrorWhenLocationIsNull() {
            var volumeResult = Volume.create(50);

            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(), null);

            assertTrue(assignmentResult.isFailure());
            assertNotNull(assignmentResult.getError());
        }
    }

    @Nested
    @DisplayName("Завершение Assignment")
    class CompletionTests {

        @Test
        @DisplayName("Должен успешно завершаться, если курьер находится в той же клетке (расстояние 0)")
        void shouldCompleteWhenCourierIsAtSameLocation() {
            var volumeResult = Volume.create(50);
            var locationResult = Location.create(5, 5);
            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    locationResult.getValue());

            var assignment = assignmentResult.getValue();
            var courierLocationResult = Location.create(5, 5);

            var completeResult = assignment.complete(courierLocationResult.getValue());

            assertTrue(completeResult.isSuccess());
            assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        }

        @Test
        @DisplayName("Должен успешно завершаться, если курьер находится на расстоянии 1")
        void shouldCompleteWhenCourierIsAtDistanceOne() {
            var volumeResult = Volume.create(50);
            var orderLocationResult = Location.create(5, 5);
            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    orderLocationResult.getValue());

            var assignment = assignmentResult.getValue();
            // Курьер на расстоянии 1 (например, 5,6 или 6,5)
            var courierLocationResult = Location.create(5, 6);

            var completeResult = assignment.complete(courierLocationResult.getValue());

            assertTrue(completeResult.isSuccess());
            assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку, если курьер находится на расстоянии больше 1")
        void shouldReturnErrorWhenCourierIsTooFar() {
            var volumeResult = Volume.create(50);
            var orderLocationResult = Location.create(5, 5);
            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    orderLocationResult.getValue());

            var assignment = assignmentResult.getValue();
            // Курьер на расстоянии 2 (например, 5,7 или 7,5)
            var courierLocationResult = Location.create(5, 7);

            var completeResult = assignment.complete(courierLocationResult.getValue());

            assertTrue(completeResult.isFailure());
            assertNotNull(completeResult.getError());
            assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при попытке завершить уже завершенное назначение")
        void shouldReturnErrorWhenAlreadyCompleted() {
            var volumeResult = Volume.create(50);
            var orderLocationResult = Location.create(5, 5);
            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    orderLocationResult.getValue());

            var assignment = assignmentResult.getValue();
            // Сначала завершаем
            var courierLocationResult1 = Location.create(5, 5);
            assignment.complete(courierLocationResult1.getValue());

            // Пытаемся завершить снова
            var courierLocationResult2 = Location.create(5, 5);
            var completeResult = assignment.complete(courierLocationResult2.getValue());

            assertTrue(completeResult.isFailure());
            assertNotNull(completeResult.getError());
        }

        @Test
        @DisplayName("Должен выбрасывать NullPointerException при null courierLocation")
        void shouldThrowExceptionWhenCourierLocationIsNull() {
            var volumeResult = Volume.create(50);
            var locationResult = Location.create(5, 5);
            var assignmentResult = Assignment.create(TEST_ID, TEST_ORDER_ID, volumeResult.getValue(),
                    locationResult.getValue());

            var assignment = assignmentResult.getValue();

            assertThrows(NullPointerException.class, () -> assignment.complete(null));
        }
    }
}
