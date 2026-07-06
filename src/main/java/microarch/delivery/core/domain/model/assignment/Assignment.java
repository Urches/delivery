package microarch.delivery.core.domain.model.assignment;

import libs.ddd.BaseEntity;
import libs.errs.*;
import libs.errs.Error;
import libs.util.ReasonedResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.Objects;
import java.util.UUID;

/**
 * Assignment - Entity, представляющая назначение заказа на курьера.
 * <p>
 * Бизнес-правила: - Assignment может быть создан только с установленным OrderId, Volume, Location и в статусе
 * "Assigned" - Assignment можно завершить только, если курьер находится в одной клетке или ближе от Location заказа
 */
@Getter
public class Assignment extends BaseEntity<UUID> {

    private final UUID orderId;
    private final Volume volume;
    private final Location location;
    private AssignmentStatus status;

    /**
     * Конструктор для использования в фабричном методе create и в инфраструктуре (маппинг).
     */
    private Assignment(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    /**
     * Создаёт Assignment с указанными параметрами. Используется инфраструктурой для восстановления из БД.
     */
    public static Assignment of(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        return new Assignment(id, orderId, volume, location, status);
    }

    /**
     * Фабричный метод для создания Assignment.
     * <p>
     * Assignment может быть создан только с установленным OrderId, Volume, Location. Статус автоматически
     * устанавливается в ASSIGNED.
     *
     * @param id
     *            уникальный идентификатор назначения (UUID)
     * @param orderId
     *            идентификатор заказа (UUID)
     * @param volume
     *            объем заказа (Volume)
     * @param location
     *            местоположение заказа (Location)
     *
     * @return Result с Assignment при успехе или Error при неудаче
     */
    public static Result<Assignment, Error> create(UUID id, UUID orderId, Volume volume, Location location) {
        var error = Guard.combine(Guard.againstNullOrEmpty(id, "id"), Guard.againstNullOrEmpty(orderId, "orderId"),
                volume == null ? GeneralErrors.valueIsRequired("volume") : null,
                location == null ? GeneralErrors.valueIsRequired("location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(id, orderId, volume, location, AssignmentStatus.ASSIGNED));
    }

    public static Assignment mustCreate(UUID id, UUID orderId, Volume volume, Location location) {
        return create(id, orderId, volume, location).getValueOrThrow();
    }

    /**
     * Завершает назначение, если курьер находится достаточно близко к месту доставки.
     * <p>
     * Assignment можно завершить только, если курьер находится в одной клетке или ближе от Location заказа.
     *
     * @param courierLocation
     *            текущее местоположение курьера
     *
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> complete(Location courierLocation) {
        Objects.requireNonNull(courierLocation, "Courier location must not be null");
        var result = canBeCompleted(courierLocation);

        var canBeCompleted = result.getValue();
        if (canBeCompleted) {
            this.status = AssignmentStatus.COMPLETED;
            return Result.success();
        } else {
            return Result.failure(GeneralErrors
                    .invalidOperation(String.format("Cannot complete assignment. Reasons: %s", result.getReasons())));
        }
    }

    /**
     * Проверяет, может ли Assignment быть завершен с данным местоположением курьера.
     * <p>
     * Assignment может быть завершен, если: - статус не COMPLETED - курьер находится на расстоянии не более 1 от
     * Location заказа
     *
     * @param courierLocation
     *            текущее местоположение курьера
     *
     * @return ReasonedResult с true при успехе или причинами отказа при неудаче
     */
    public ReasonedResult<Boolean> canBeCompleted(Location courierLocation) {
        Objects.requireNonNull(courierLocation, "Courier location must not be null");

        if (this.status == AssignmentStatus.COMPLETED) {
            return ReasonedResult.withReason(false, "Assignment is already completed");
        }
        int distance = this.location.distanceTo(courierLocation);
        if (distance > 1) {
            return ReasonedResult.withReason(false,
                    String.format("Courier is too far from order location. Distance: %d, max allowed: 1", distance));
        }
        return ReasonedResult.withNoReason(true);
    }
}
