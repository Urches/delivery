package microarch.delivery.core.domain.model.order;

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
 * Volume - Value Object, представляющий объем заказа. Диапазон допустимых значений: от 1 до 100 включительно.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Volume extends ValueObject<Volume> {

    private static final int MIN_VOLUME = 1;
    private static final int MAX_VOLUME = 100;

    private final int value;

    /**
     * Фабричный метод для создания Volume.
     *
     * @param value значение объема (от 1 до 100)
     * @return Result с Volume при успехе или Error при неудаче
     */
    public static Result<Volume, Error> create(int value) {
        var error = Guard.againstOutOfRange(value, MIN_VOLUME, MAX_VOLUME, "volume");
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Volume(value));
    }

    /**
     * Метод для создания нового Volume, содержащего value текущего Volume + value входного Volume.
     *
     * @param volume добавочное значение Volume
     * @return Result с Volume при успехе или Error при неудаче. Если volume == null - ошибка
     */
    public Result<Volume, Error> plus(Volume volume) {
        Objects.requireNonNull(volume, "Volume must not be null");

        return Volume.create(this.value + volume.value);
    }

    /**
     * @param volume Volume для сравнения
     * @return true если текущий Volume меньше или равен volume
     */
    public boolean lessOrEqual(Volume volume) {
        return compareTo(volume) <= 0;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
