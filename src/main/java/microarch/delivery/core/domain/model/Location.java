package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * Location - Value Object, представляющий координату на доске. Состоит из X (горизонталь) и Y (вертикаль). Диапазон
 * допустимых значений: от 1 до 10 включительно для каждой координаты.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {

    private static final int MIN_COORDINATE = 1;
    private static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    /**
     * Фабричный метод для создания Location.
     *
     * @param x
     *            координата по горизонтали (от 1 до 10)
     * @param y
     *            координата по вертикали (от 1 до 10)
     *
     * @return Result с Location при успехе или Error при неудаче
     */
    public static Result<Location, Error> create(int x, int y) {
        var error = validateCoordinates(x, y);
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Location(x, y));
    }

    /**
     * Рассчитывает расстояние до другой точки Location. Расстояние - это совокупное количество шагов по X и Y
     * (манхэттенское расстояние).
     *
     * @param other
     *            другая точка Location
     *
     * @return количество шагов, необходимых для достижения точки other
     */
    public int distanceTo(Location other) {
        Objects.requireNonNull(other, "Other location must not be null");
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Проверяет валидность координат.
     *
     * @param x
     *            координата X
     * @param y
     *            координата Y
     *
     * @return Error если координаты невалидны, null если валидны
     */
    private static Error validateCoordinates(int x, int y) {
        var xError = Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x");
        var yError = Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y");
        return Guard.combine(xError, yError);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
