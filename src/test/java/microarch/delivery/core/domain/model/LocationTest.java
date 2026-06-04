package microarch.delivery.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location Value Object")
class LocationTest {

    @Nested
    @DisplayName("Создание Location")
    class CreationTests {

        @Test
        @DisplayName("Должен успешно создаваться с минимальными координатами (1,1)")
        void shouldCreateLocationWithMinCoordinates() {
            var result = Location.create(1, 1);
            assertTrue(result.isSuccess());

            var location = result.getValue();
            assertNotNull(location);
            assertEquals(1, location.getX());
            assertEquals(1, location.getY());
        }

        @Test
        @DisplayName("Должен успешно создаваться с максимальными координатами (10,10)")
        void shouldCreateLocationWithMaxCoordinates() {
            var result = Location.create(10, 10);
            assertTrue(result.isSuccess());

            var location = result.getValue();
            assertNotNull(location);
            assertEquals(10, location.getX());
            assertEquals(10, location.getY());
        }

        @Test
        @DisplayName("Должен успешно создаваться с координатами в диапазоне (5,7)")
        void shouldCreateLocationWithValidCoordinates() {
            var result = Location.create(5, 7);
            assertTrue(result.isSuccess());

            var location = result.getValue();
            assertNotNull(location);
            assertEquals(5, location.getX());
            assertEquals(7, location.getY());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при X < 1")
        void shouldReturnErrorWhenXIsLessThanMin() {
            var result = Location.create(0, 5);
            assertTrue(result.isFailure());
            assertNotNull(result.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при Y < 1")
        void shouldReturnErrorWhenYIsLessThanMin() {
            var result = Location.create(5, 0);
            assertTrue(result.isFailure());
            assertNotNull(result.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при X > 10")
        void shouldReturnErrorWhenXIsGreaterThanMax() {
            var result = Location.create(11, 5);
            assertTrue(result.isFailure());
            assertNotNull(result.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при Y > 10")
        void shouldReturnErrorWhenYIsGreaterThanMax() {
            var result = Location.create(5, 11);
            assertTrue(result.isFailure());
            assertNotNull(result.getError());
        }

        @Test
        @DisplayName("Должен возвращать ошибку при обеих координатах вне диапазона")
        void shouldReturnErrorWhenBothCoordinatesOutOfRange() {
            var result = Location.create(0, 15);
            assertTrue(result.isFailure());
            assertNotNull(result.getError());
        }
    }

    @Nested
    @DisplayName("Расстояние между Location")
    class DistanceTests {

        @Test
        @DisplayName("Должен возвращать 0 для одинаковых точек")
        void shouldReturnZeroForSameLocation() {
            var result1 = Location.create(5, 5);
            var result2 = Location.create(5, 5);

            var distance = result1.getValue().distanceTo(result2.getValue());

            assertEquals(0, distance);
        }

        @Test
        @DisplayName("Должен возвращать правильное расстояние (пример из задачи: 2 шага по X + 3 шага по Y = 5)")
        void shouldCalculateCorrectDistance() {
            var courierResult = Location.create(1, 1);
            var orderResult = Location.create(3, 4);

            var distance = courierResult.getValue().distanceTo(orderResult.getValue());

            assertEquals(5, distance);
        }

        @Test
        @DisplayName("Должен возвращать расстояние только по X")
        void shouldCalculateDistanceOnlyByX() {
            var result1 = Location.create(1, 5);
            var result2 = Location.create(8, 5);

            var distance = result1.getValue().distanceTo(result2.getValue());

            assertEquals(7, distance);
        }

        @Test
        @DisplayName("Должен возвращать расстояние только по Y")
        void shouldCalculateDistanceOnlyByY() {
            var result1 = Location.create(5, 1);
            var result2 = Location.create(5, 9);

            var distance = result1.getValue().distanceTo(result2.getValue());

            assertEquals(8, distance);
        }

        @Test
        @DisplayName("Должен возвращать максимальное расстояние (от 1,1 до 10,10)")
        void shouldCalculateMaxDistance() {
            var result1 = Location.create(1, 1);
            var result2 = Location.create(10, 10);

            var distance = result1.getValue().distanceTo(result2.getValue());

            assertEquals(18, distance);
        }

        @Test
        @DisplayName("Должен выбрасывать NullPointerException при null аргументе")
        void shouldThrowExceptionForNullArgument() {
            var result = Location.create(5, 5);
            var location = result.getValue();
            assertThrows(NullPointerException.class, () -> location.distanceTo(null));
        }
    }
}
