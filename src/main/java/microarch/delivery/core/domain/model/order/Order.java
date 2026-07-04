package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

/**
 * Order - Aggregate Root, представляющий заказ на доставку.
 * <p>
 * Бизнес-правила: - Order состоит из: Id, Location, Volume, Status - Заказ может быть создан при передаче Id, Location,
 * Volume - При создании заказа проставляется статус CREATED - Заказ может сменить статус на ASSIGNED, но только если
 * его текущий статус CREATED - Заказ может сменить статус на COMPLETED, но только если его текущий статус ASSIGNED
 */
@Getter
public class Order extends Aggregate<UUID> {

    private final Location location;
    private final Volume volume;
    private OrderStatus status;

    /**
     * Конструктор для использования в фабричном методе create.
     */
    private Order(UUID id, Location location, Volume volume, OrderStatus status) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    /**
     * Создаёт Order с указанным статусом. Используется инфраструктурой для восстановления из БД.
     */
    public static Order of(UUID id, Location location, Volume volume, OrderStatus status) {
        return new Order(id, location, volume, status);
    }

    /**
     * Фабричный метод для создания Order.
     * <p>
     * Заказ может быть создан при передаче Id, Location, Volume. При создании заказа проставляется статус CREATED.
     *
     * @param id
     *            уникальный идентификатор заказа (UUID)
     * @param location
     *            местоположение доставки (Location)
     * @param volume
     *            объем заказа (Volume)
     *
     * @return Result с Order при успехе или Error при неудаче
     */
    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        var error = Guard.combine(
                Guard.againstNullOrEmpty(id, "id"),
                location == null ? GeneralErrors.valueIsRequired("location") : null,
                volume == null ? GeneralErrors.valueIsRequired("volume") : null);
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Order(id, location, volume, OrderStatus.CREATED));
    }

    public static Order mustCreate(UUID id, Location location, Volume volume) {
        return create(id, location, volume).getValueOrThrow();
    }

    /**
     * Меняет статус заказа на ASSIGNED.
     * <p>
     * Заказ может сменить статус на ASSIGNED, но только если его текущий статус CREATED.
     *
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> assign() {
        if (this.status != OrderStatus.CREATED) {
            return Result.failure(GeneralErrors.invalidOperation(
                    String.format("Cannot assign order: current status is %s, expected CREATED", this.status)));
        }

        this.status = OrderStatus.ASSIGNED;
        return Result.success();
    }

    /**
     * Меняет статус заказа на COMPLETED.
     * <p>
     * Заказ может сменить статус на COMPLETED, но только если его текущий статус ASSIGNED.
     *
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> complete() {
        if (this.status != OrderStatus.ASSIGNED) {
            return Result.failure(GeneralErrors.invalidOperation(
                    String.format("Cannot complete order: current status is %s, expected ASSIGNED", this.status)));
        }

        this.status = OrderStatus.COMPLETED;
        return Result.success();
    }
}
